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
import com.yuan.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 成绩查询 Service
 */
@Service
public class ScoreService {

    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final UserRepository userRepository;

    public ScoreService(ExamRecordRepository examRecordRepository,
                        AnswerRecordRepository answerRecordRepository,
                        UserRepository userRepository) {
        this.examRecordRepository = examRecordRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 列出当前学生的所有成绩
     */
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

    /**
     * 查询单次作答的成绩明细
     */
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
        if (!record.getUser().getId().equals(user.getId())) {
            return Result.error(403, "无权查看他人成绩");
        }
        Exam exam = record.getExam();

        List<AnswerRecord> answers = answerRecordRepository.findByExamRecord_Id(examRecordId);

        List<AnswerDetailVo> details = answers.stream()
                .map(ar -> {
                    Question q = ar.getQuestion();
                    AnswerDetailVo d = new AnswerDetailVo();
                    d.setQuestionId(q.getId());
                    d.setContent(q.getContent());
                    d.setType(q.getType());
                    d.setOptions(q.getOptions());
                    d.setStudentAnswer(ar.getAnswer());
                    d.setCorrectAnswer(q.getAnswer());
                    d.setIsCorrect(ar.getIsCorrect());
                    d.setScore(ar.getScore());
                    d.setMaxScore(q.getScore());
                    return d;
                })
                .toList();

        ScoreVo vo = new ScoreVo();
        vo.setExamRecordId(record.getId());
        vo.setExamId(exam.getId());
        vo.setExamName(exam.getName());
        vo.setTotalScore(record.getTotalScore());
        vo.setExamMaxScore(exam.getTotalScore());
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
        vo.setExamMaxScore(r.getExam().getTotalScore());
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
