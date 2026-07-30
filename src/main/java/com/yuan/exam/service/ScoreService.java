package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.AnswerDetailVo;
import com.yuan.exam.dto.ExamRecordVo;
import com.yuan.exam.dto.ExamStatsVo;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.entity.AnswerRecord;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.ExamRecord;
import com.yuan.exam.entity.ExamStatus;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.QuestionSnapshot;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.AnswerRecordRepository;
import com.yuan.exam.repository.ExamRecordRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionSnapshotRepository;
import com.yuan.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成绩查询（学生本人 + 教师全员/统计）
 */
@Service
public class ScoreService {

    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public ScoreService(ExamRecordRepository examRecordRepository,
                        AnswerRecordRepository answerRecordRepository,
                        QuestionSnapshotRepository questionSnapshotRepository,
                        ExamRepository examRepository,
                        UserRepository userRepository) {
        this.examRecordRepository = examRecordRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.questionSnapshotRepository = questionSnapshotRepository;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Result<List<ExamRecordVo>> myScores() {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        List<ExamRecord> records = examRecordRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
        List<ExamRecordVo> list = records.stream()
                .map(r -> toVo(r, r.getExam().getName(), user.getUsername()))
                .toList();
        return Result.success(list);
    }

    @Transactional(readOnly = true)
    public Result<List<ExamRecordVo>> examScores(Long examId) {
        if (!examRepository.existsById(examId)) {
            return Result.error(404, "考试不存在");
        }
        List<ExamRecordVo> list = examRecordRepository.findByExam_IdOrderByStartTimeDesc(examId).stream()
                .map(r -> toVo(r, r.getExam().getName(), r.getUser().getUsername()))
                .toList();
        return Result.success(list);
    }

    @Transactional(readOnly = true)
    public Result<ExamStatsVo> examStats(Long examId) {
        Exam exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            return Result.error(404, "考试不存在");
        }
        List<ExamRecord> records = examRecordRepository.findByExam_IdOrderByStartTimeDesc(examId);
        List<ExamRecord> graded = records.stream()
                .filter(r -> r.getStatus() == ExamStatus.GRADED || r.getStatus() == ExamStatus.TIMEOUT_AUTO_GRADED)
                .filter(r -> r.getTotalScore() != null)
                .toList();

        ExamStatsVo vo = new ExamStatsVo();
        vo.setExamId(examId);
        vo.setExamName(exam.getName());
        vo.setParticipantCount(records.size());
        vo.setGradedCount(graded.size());
        vo.setExamMaxScore(exam.getTotalScore());
        if (!graded.isEmpty()) {
            DoubleSummaryStatistics stats = graded.stream()
                    .mapToDouble(r -> r.getTotalScore())
                    .summaryStatistics();
            vo.setAverageScore(Math.round(stats.getAverage() * 100.0) / 100.0);
            vo.setMaxScore((int) stats.getMax());
            vo.setMinScore((int) stats.getMin());
        }
        return Result.success(vo);
    }

    @Transactional(readOnly = true)
    public Result<ScoreVo> detail(Long examRecordId) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        var recordOpt = examRecordRepository.findById(examRecordId);
        if (recordOpt.isEmpty()) {
            return Result.error(404, "作答记录不存在");
        }
        ExamRecord record = recordOpt.get();
        boolean staff = SecurityUtils.isStaff();
        if (!staff && !record.getUser().getId().equals(user.getId())) {
            return Result.error(403, "无权查看他人成绩");
        }
        Exam exam = record.getExam();

        List<QuestionSnapshot> snapshots = questionSnapshotRepository.findByExamRecord_Id(examRecordId);
        Map<Long, QuestionSnapshot> snapMap = snapshots.stream()
                .collect(Collectors.toMap(QuestionSnapshot::getSourceQuestionId, s -> s, (a, b) -> a));

        List<AnswerRecord> answers = answerRecordRepository.findByExamRecord_Id(examRecordId);
        List<AnswerDetailVo> details = answers.stream()
                .map(ar -> {
                    Question q = ar.getQuestion();
                    QuestionSnapshot snap = snapMap.get(q.getId());
                    AnswerDetailVo d = new AnswerDetailVo();
                    d.setQuestionId(q.getId());
                    d.setContent(snap != null ? snap.getContent() : q.getContent());
                    d.setType(snap != null ? snap.getType() : q.getType());
                    d.setOptions(snap != null ? snap.getOptions() : q.getOptions());
                    d.setStudentAnswer(ar.getAnswer());
                    d.setCorrectAnswer(snap != null ? snap.getAnswer() : q.getAnswer());
                    d.setIsCorrect(ar.getIsCorrect());
                    d.setScore(ar.getScore());
                    d.setMaxScore(snap != null ? snap.getScore() : q.getScore());
                    return d;
                })
                .toList();

        int examMaxScore = snapshots.isEmpty()
                ? (exam.getTotalScore() == null ? 0 : exam.getTotalScore())
                : snapshots.stream().mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).sum();

        ScoreVo vo = new ScoreVo();
        vo.setExamRecordId(record.getId());
        vo.setExamId(exam.getId());
        vo.setExamName(exam.getName());
        vo.setTotalScore(record.getTotalScore());
        vo.setExamMaxScore(examMaxScore);
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setStatus(record.getStatus());
        vo.setDetails(details);
        return Result.success(vo);
    }

    private ExamRecordVo toVo(ExamRecord r, String examName, String username) {
        ExamRecordVo vo = new ExamRecordVo();
        vo.setId(r.getId());
        vo.setExamId(r.getExam().getId());
        vo.setExamName(examName);
        vo.setUserId(r.getUser().getId());
        vo.setUsername(username);
        vo.setStartTime(r.getStartTime());
        vo.setSubmitTime(r.getSubmitTime());
        vo.setTotalScore(r.getTotalScore());
        vo.setStatus(r.getStatus());
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
