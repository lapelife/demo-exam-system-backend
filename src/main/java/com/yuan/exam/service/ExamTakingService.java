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
import com.yuan.exam.entity.QuestionType;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.AnswerRecordRepository;
import com.yuan.exam.repository.ExamRecordRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 學生作答 Service：開始作答、提交並自動判分
 */
@Service
public class ExamTakingService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final UserRepository userRepository;

    public ExamTakingService(ExamRepository examRepository,
                             QuestionRepository questionRepository,
                             ExamRecordRepository examRecordRepository,
                             AnswerRecordRepository answerRecordRepository,
                             UserRepository userRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examRecordRepository = examRecordRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 開始作答：建立 ExamRecord 並回傳題目（不含答案）
     */
    @Transactional
    public Result<StartExamVo> startExam(Long examId) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登入");
        }

        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return Result.error(404, "考試不存在");
        }
        Exam exam = examOpt.get();

        // 檢查是否已有作答記錄
        List<ExamRecord> existing = examRecordRepository.findByExamIdAndUserId(examId, user.getId());
        if (!existing.isEmpty()) {
            ExamRecord latest = existing.get(0);
            if (latest.getStatus() != ExamStatus.IN_PROGRESS) {
                return Result.error(400, "此考試已完成，不可重複作答");
            }
            // 進行中：回傳既有記錄
            return Result.success(buildStartVo(latest, exam));
        }

        // 建立新作答記錄
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(user.getId());
        record.setStartTime(LocalDateTime.now());
        record.setStatus(ExamStatus.IN_PROGRESS);
        record = examRecordRepository.save(record);

        return Result.success(buildStartVo(record, exam));
    }

    /**
     * 提交作答並自動判分
     */
    @Transactional
    public Result<ScoreVo> submitExam(SubmitAnswerDto dto) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登入");
        }

        Optional<ExamRecord> recordOpt = examRecordRepository.findById(dto.getExamRecordId());
        if (recordOpt.isEmpty()) {
            return Result.error(404, "作答記錄不存在");
        }
        ExamRecord record = recordOpt.get();
        if (!record.getUserId().equals(user.getId())) {
            return Result.error(403, "無權操作他人作答記錄");
        }
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return Result.error(400, "此作答已提交，不可重複提交");
        }

        List<Question> questions = questionRepository.findByExamId(record.getExamId());
        // 題目 id → Question 對照
        var qMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        int totalScore = 0;
        int maxScore = 0;
        List<AnswerDetailVo> details = new ArrayList<>();

        for (AnswerDto a : dto.getAnswers()) {
            Question q = qMap.get(a.getQuestionId());
            if (q == null) {
                continue;
            }
            maxScore += q.getScore();
            boolean correct = isCorrect(q, a.getAnswer());
            int score = correct ? q.getScore() : 0;
            totalScore += score;

            // 寫入單題作答記錄
            AnswerRecord ar = new AnswerRecord();
            ar.setExamRecordId(record.getId());
            ar.setQuestionId(q.getId());
            ar.setAnswer(a.getAnswer());
            ar.setIsCorrect(correct);
            ar.setScore(score);
            answerRecordRepository.save(ar);

            details.add(toDetail(q, a.getAnswer(), correct, score));
        }

        // 更新作答記錄
        record.setSubmitTime(LocalDateTime.now());
        record.setTotalScore(totalScore);
        record.setStatus(ExamStatus.GRADED);
        examRecordRepository.save(record);

        // 組裝成績回傳
        Exam exam = examRepository.findById(record.getExamId()).orElse(null);
        ScoreVo vo = new ScoreVo();
        vo.setExamRecordId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setExamName(exam == null ? "" : exam.getName());
        vo.setTotalScore(totalScore);
        vo.setExamMaxScore(maxScore);
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setStatus(record.getStatus());
        vo.setDetails(details);
        return Result.success(vo);
    }

    /**
     * 判分邏輯：單選/判斷精確比對；多選集合比對（全對才給分）
     */
    private boolean isCorrect(Question q, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return false;
        }
        if (q.getType() == QuestionType.MULTI) {
            Set<String> correct = splitToSet(q.getAnswer());
            Set<String> student = splitToSet(studentAnswer);
            return correct.equals(student);
        }
        // SINGLE / JUDGE
        return q.getAnswer().trim().equalsIgnoreCase(studentAnswer.trim());
    }

    private Set<String> splitToSet(String s) {
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toSet());
    }

    private AnswerDetailVo toDetail(Question q, String studentAnswer, boolean correct, int score) {
        AnswerDetailVo d = new AnswerDetailVo();
        d.setQuestionId(q.getId());
        d.setContent(q.getContent());
        d.setType(q.getType());
        d.setOptions(q.getOptions());
        d.setStudentAnswer(studentAnswer);
        d.setCorrectAnswer(q.getAnswer());
        d.setIsCorrect(correct);
        d.setScore(score);
        d.setMaxScore(q.getScore());
        return d;
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
        vo.setExam(examVo);

        List<QuestionVo> questions = questionRepository.findByExamId(exam.getId()).stream()
                .map(q -> {
                    QuestionVo qv = new QuestionVo();
                    qv.setId(q.getId());
                    qv.setExamId(q.getExamId());
                    qv.setType(q.getType());
                    qv.setContent(q.getContent());
                    qv.setOptions(q.getOptions());
                    // 學生視圖隱藏答案
                    qv.setAnswer(null);
                    qv.setScore(q.getScore());
                    return qv;
                })
                .toList();
        vo.setQuestions(questions);
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
