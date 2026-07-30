package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.AnswerDetailVo;
import com.yuan.exam.dto.AnswerDto;
import com.yuan.exam.dto.ExamVo;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.dto.StartExamVo;
import com.yuan.exam.dto.SubmitAnswerDto;
import com.yuan.exam.entity.AnswerRecord;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.ExamRecord;
import com.yuan.exam.entity.ExamStatus;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.QuestionSnapshot;
import com.yuan.exam.entity.QuestionType;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.AnswerRecordRepository;
import com.yuan.exam.repository.ExamRecordRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.QuestionSnapshotRepository;
import com.yuan.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生作答：开始、草稿、提交；超时自动判分；开考锁题快照
 */
@Service
public class ExamTakingService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final UserRepository userRepository;

    public ExamTakingService(ExamRepository examRepository,
                             QuestionRepository questionRepository,
                             ExamRecordRepository examRecordRepository,
                             AnswerRecordRepository answerRecordRepository,
                             QuestionSnapshotRepository questionSnapshotRepository,
                             UserRepository userRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examRecordRepository = examRecordRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Result<StartExamVo> startExam(Long examId) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }

        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        Exam exam = examOpt.get();
        LocalDateTime now = LocalDateTime.now();

        Result<Void> windowCheck = checkExamWindow(exam, now);
        if (windowCheck.getCode() != 200) {
            return Result.error(windowCheck.getCode(), windowCheck.getMsg());
        }

        List<ExamRecord> existing = examRecordRepository.findByExam_IdAndUser_Id(examId, user.getId());
        if (!existing.isEmpty()) {
            ExamRecord latest = existing.get(0);
            if (latest.getStatus() != ExamStatus.IN_PROGRESS) {
                return Result.error(400, "此考试已完成，不可重复作答");
            }
            // 已超时：自动交卷
            if (isTimedOut(exam, latest, now)) {
                ScoreVo score = gradeRecord(latest, collectDraftAnswers(latest.getId()), true);
                return Result.error(400, "作答已超时并已自动交卷，得分：" + score.getTotalScore());
            }
            return Result.success(buildStartVo(latest, exam));
        }

        ExamRecord record = new ExamRecord();
        record.setExam(exam);
        record.setUser(user);
        record.setStartTime(now);
        record.setStatus(ExamStatus.IN_PROGRESS);
        record = examRecordRepository.save(record);
        createSnapshots(record, exam.getId());

        return Result.success(buildStartVo(record, exam));
    }

    /**
     * 保存草稿（不判分）
     */
    @Transactional
    public Result<Void> saveDraft(SubmitAnswerDto dto) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        Optional<ExamRecord> recordOpt = examRecordRepository.findById(dto.getExamRecordId());
        if (recordOpt.isEmpty()) {
            return Result.error(404, "作答记录不存在");
        }
        ExamRecord record = recordOpt.get();
        if (!record.getUser().getId().equals(user.getId())) {
            return Result.error(403, "无权操作他人作答记录");
        }
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return Result.error(400, "此作答已提交");
        }
        if (isTimedOut(record.getExam(), record, LocalDateTime.now())) {
            gradeRecord(record, dto.getAnswers() == null ? List.of() : dto.getAnswers(), true);
            return Result.error(400, "作答已超时并已自动交卷");
        }

        Map<Long, Question> qMap = questionRepository.findByExam_Id(record.getExam().getId()).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        if (dto.getAnswers() != null) {
            for (AnswerDto a : dto.getAnswers()) {
                Question q = qMap.get(a.getQuestionId());
                if (q == null) {
                    continue;
                }
                upsertDraft(record, q, a.getAnswer());
            }
        }
        return Result.success();
    }

    @Transactional
    public Result<ScoreVo> submitExam(SubmitAnswerDto dto) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }

        Optional<ExamRecord> recordOpt = examRecordRepository.findById(dto.getExamRecordId());
        if (recordOpt.isEmpty()) {
            return Result.error(404, "作答记录不存在");
        }
        ExamRecord record = recordOpt.get();
        if (!record.getUser().getId().equals(user.getId())) {
            return Result.error(403, "无权操作他人作答记录");
        }
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return Result.error(400, "此作答已提交，不可重复提交");
        }

        boolean timedOut = isTimedOut(record.getExam(), record, LocalDateTime.now());
        List<AnswerDto> answers = dto.getAnswers() == null ? List.of() : dto.getAnswers();
        ScoreVo vo = gradeRecord(record, answers, timedOut);
        return Result.success(vo);
    }

    private ScoreVo gradeRecord(ExamRecord record, List<AnswerDto> submitted, boolean timedOut) {
        Exam exam = record.getExam();
        List<QuestionSnapshot> snapshots = questionSnapshotRepository.findByExamRecord_Id(record.getId());
        if (snapshots.isEmpty()) {
            createSnapshots(record, exam.getId());
            snapshots = questionSnapshotRepository.findByExamRecord_Id(record.getId());
        }

        Map<Long, String> answerMap = new HashMap<>();
        for (AnswerDto a : submitted) {
            if (a.getQuestionId() != null) {
                answerMap.put(a.getQuestionId(), a.getAnswer());
            }
        }
        // 合并已有草稿
        for (AnswerRecord ar : answerRecordRepository.findByExamRecord_Id(record.getId())) {
            answerMap.putIfAbsent(ar.getQuestion().getId(), ar.getAnswer());
        }

        int totalScore = 0;
        int maxScore = 0;
        List<AnswerDetailVo> details = new ArrayList<>();

        for (QuestionSnapshot snap : snapshots) {
            maxScore += snap.getScore() == null ? 0 : snap.getScore();
            String studentAnswer = answerMap.getOrDefault(snap.getSourceQuestionId(), "");
            boolean correct = isCorrect(snap.getType(), snap.getAnswer(), studentAnswer);
            int score = correct ? (snap.getScore() == null ? 0 : snap.getScore()) : 0;
            totalScore += score;

            Question questionRef = questionRepository.findById(snap.getSourceQuestionId()).orElse(null);
            if (questionRef != null) {
                AnswerRecord ar = answerRecordRepository
                        .findByExamRecord_IdAndQuestion_Id(record.getId(), questionRef.getId())
                        .orElseGet(AnswerRecord::new);
                ar.setExamRecord(record);
                ar.setQuestion(questionRef);
                ar.setAnswer(studentAnswer);
                ar.setIsCorrect(correct);
                ar.setScore(score);
                answerRecordRepository.save(ar);
            }

            AnswerDetailVo d = new AnswerDetailVo();
            d.setQuestionId(snap.getSourceQuestionId());
            d.setContent(snap.getContent());
            d.setType(snap.getType());
            d.setOptions(snap.getOptions());
            d.setStudentAnswer(studentAnswer);
            d.setCorrectAnswer(snap.getAnswer());
            d.setIsCorrect(correct);
            d.setScore(score);
            d.setMaxScore(snap.getScore());
            details.add(d);
        }

        LocalDateTime now = LocalDateTime.now();
        record.setSubmitTime(now);
        record.setTotalScore(totalScore);
        record.setStatus(timedOut ? ExamStatus.TIMEOUT_AUTO_GRADED : ExamStatus.GRADED);
        examRecordRepository.save(record);

        ScoreVo vo = new ScoreVo();
        vo.setExamRecordId(record.getId());
        vo.setExamId(exam.getId());
        vo.setExamName(exam.getName());
        vo.setTotalScore(totalScore);
        vo.setExamMaxScore(maxScore);
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setStatus(record.getStatus());
        vo.setDetails(details);
        return vo;
    }

    private void createSnapshots(ExamRecord record, Long examId) {
        List<Question> questions = questionRepository.findByExam_Id(examId);
        for (Question q : questions) {
            QuestionSnapshot snap = new QuestionSnapshot();
            snap.setExamRecord(record);
            snap.setSourceQuestionId(q.getId());
            snap.setType(q.getType());
            snap.setContent(q.getContent());
            snap.setOptions(q.getOptions());
            snap.setAnswer(q.getAnswer());
            snap.setScore(q.getScore());
            questionSnapshotRepository.save(snap);
        }
    }

    private void upsertDraft(ExamRecord record, Question q, String answer) {
        AnswerRecord ar = answerRecordRepository
                .findByExamRecord_IdAndQuestion_Id(record.getId(), q.getId())
                .orElseGet(AnswerRecord::new);
        ar.setExamRecord(record);
        ar.setQuestion(q);
        ar.setAnswer(answer == null ? "" : answer);
        ar.setIsCorrect(null);
        ar.setScore(null);
        answerRecordRepository.save(ar);
    }

    private List<AnswerDto> collectDraftAnswers(Long examRecordId) {
        return answerRecordRepository.findByExamRecord_Id(examRecordId).stream()
                .map(ar -> {
                    AnswerDto d = new AnswerDto();
                    d.setQuestionId(ar.getQuestion().getId());
                    d.setAnswer(ar.getAnswer());
                    return d;
                })
                .toList();
    }

    private boolean isTimedOut(Exam exam, ExamRecord record, LocalDateTime now) {
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            return true;
        }
        if (exam.getDuration() != null && record.getStartTime() != null) {
            LocalDateTime deadline = record.getStartTime().plusMinutes(exam.getDuration());
            return now.isAfter(deadline);
        }
        return false;
    }

    private LocalDateTime resolveDeadline(Exam exam, ExamRecord record) {
        LocalDateTime byDuration = null;
        if (exam.getDuration() != null && record.getStartTime() != null) {
            byDuration = record.getStartTime().plusMinutes(exam.getDuration());
        }
        LocalDateTime byEnd = exam.getEndTime();
        if (byDuration == null) {
            return byEnd;
        }
        if (byEnd == null) {
            return byDuration;
        }
        return byDuration.isBefore(byEnd) ? byDuration : byEnd;
    }

    private Result<Void> checkExamWindow(Exam exam, LocalDateTime now) {
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            return Result.error(400, "考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            return Result.error(400, "考试已结束");
        }
        return Result.success();
    }

    private boolean isCorrect(QuestionType type, String correctAnswer, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return false;
        }
        if (correctAnswer == null) {
            return false;
        }
        if (type == QuestionType.MULTI) {
            return splitToSet(correctAnswer).equals(splitToSet(studentAnswer));
        }
        return correctAnswer.trim().equalsIgnoreCase(studentAnswer.trim());
    }

    private Set<String> splitToSet(String s) {
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toSet());
    }

    private StartExamVo buildStartVo(ExamRecord record, Exam exam) {
        StartExamVo vo = new StartExamVo();
        vo.setExamRecordId(record.getId());

        ExamVo examVo = new ExamVo();
        examVo.setId(exam.getId());
        examVo.setName(exam.getName());
        examVo.setDuration(exam.getDuration());
        examVo.setTotalScore(exam.getTotalScore());
        examVo.setStartTime(exam.getStartTime());
        examVo.setEndTime(exam.getEndTime());
        examVo.setWindowStatus(ExamService.resolveWindowStatus(exam, LocalDateTime.now()));
        vo.setExam(examVo);

        List<QuestionSnapshot> snapshots = questionSnapshotRepository.findByExamRecord_Id(record.getId());
        if (snapshots.isEmpty()) {
            createSnapshots(record, exam.getId());
            snapshots = questionSnapshotRepository.findByExamRecord_Id(record.getId());
        }

        List<QuestionVo> questions = snapshots.stream()
                .map(s -> {
                    QuestionVo qv = new QuestionVo();
                    qv.setId(s.getSourceQuestionId());
                    qv.setExamId(exam.getId());
                    qv.setType(s.getType());
                    qv.setContent(s.getContent());
                    qv.setOptions(s.getOptions());
                    qv.setAnswer(null);
                    qv.setScore(s.getScore());
                    return qv;
                })
                .toList();
        vo.setQuestions(questions);

        LocalDateTime deadline = resolveDeadline(exam, record);
        vo.setDeadline(deadline);
        if (deadline != null) {
            long remain = Duration.between(LocalDateTime.now(), deadline).getSeconds();
            vo.setRemainSeconds(Math.max(0, remain));
        } else {
            vo.setRemainSeconds(exam.getDuration() == null ? 0L : exam.getDuration() * 60L);
        }

        vo.setSavedAnswers(collectDraftAnswers(record.getId()));
        return vo;
    }

    private User currentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username);
    }
}
