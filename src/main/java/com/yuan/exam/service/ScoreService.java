package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.AnswerDetailVo;
import com.yuan.exam.dto.ExamRecordVo;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.entity.AnswerRecord;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.ExamRecord;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.AnswerRecordRepository;
import com.yuan.exam.repository.ExamRecordRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 成績查詢 Service
 */
@Service
public class ScoreService {

    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ScoreService(ExamRecordRepository examRecordRepository,
                        AnswerRecordRepository answerRecordRepository,
                        ExamRepository examRepository,
                        QuestionRepository questionRepository,
                        UserRepository userRepository) {
        this.examRecordRepository = examRecordRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 列出當前學生的所有成績
     */
    public Result<List<ExamRecordVo>> myScores() {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登入");
        }
        List<ExamRecord> records = examRecordRepository.findByUserIdOrderByStartTimeDesc(user.getId());
        Map<Long, String> examNameMap = new HashMap<>();
        for (ExamRecord r : records) {
            examNameMap.computeIfAbsent(r.getExamId(), id -> {
                Exam e = examRepository.findById(id).orElse(null);
                return e == null ? "" : e.getName();
            });
        }
        List<ExamRecordVo> list = records.stream()
                .map(r -> toVo(r, examNameMap.getOrDefault(r.getExamId(), ""), user.getUsername()))
                .toList();
        return Result.success(list);
    }

    /**
     * 查詢單次作答的成績明細
     */
    public Result<ScoreVo> detail(Long examRecordId) {
        User user = currentUser();
        if (user == null) {
            return Result.error(401, "未登入");
        }
        Optional<ExamRecord> recordOpt = examRecordRepository.findById(examRecordId);
        if (recordOpt.isEmpty()) {
            return Result.error(404, "作答記錄不存在");
        }
        ExamRecord record = recordOpt.get();
        if (!record.getUserId().equals(user.getId())) {
            return Result.error(403, "無權查看他人成績");
        }
        Exam exam = examRepository.findById(record.getExamId()).orElse(null);

        List<AnswerRecord> answers = answerRecordRepository.findByExamRecordId(examRecordId);
        Map<Long, Question> qMap = questionRepository.findByExamId(record.getExamId()).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<AnswerDetailVo> details = answers.stream()
                .map(ar -> {
                    Question q = qMap.get(ar.getQuestionId());
                    AnswerDetailVo d = new AnswerDetailVo();
                    d.setQuestionId(ar.getQuestionId());
                    d.setContent(q == null ? "" : q.getContent());
                    d.setType(q == null ? null : q.getType());
                    d.setOptions(q == null ? null : q.getOptions());
                    d.setStudentAnswer(ar.getAnswer());
                    d.setCorrectAnswer(q == null ? null : q.getAnswer());
                    d.setIsCorrect(ar.getIsCorrect());
                    d.setScore(ar.getScore());
                    d.setMaxScore(q == null ? 0 : q.getScore());
                    return d;
                })
                .toList();

        ScoreVo vo = new ScoreVo();
        vo.setExamRecordId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setExamName(exam == null ? "" : exam.getName());
        vo.setTotalScore(record.getTotalScore());
        vo.setExamMaxScore(exam == null ? 0 : exam.getTotalScore());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setStatus(record.getStatus());
        vo.setDetails(details);
        return Result.success(vo);
    }

    private ExamRecordVo toVo(ExamRecord r, String examName, String username) {
        ExamRecordVo vo = new ExamRecordVo();
        vo.setId(r.getId());
        vo.setExamId(r.getExamId());
        vo.setExamName(examName);
        vo.setUserId(r.getUserId());
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
