package com.yuan.exam.service;

import com.yuan.exam.common.PageRequests;
import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.ExamVo;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.ExamRecord;
import com.yuan.exam.entity.ExamStatus;
import com.yuan.exam.entity.Question;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.AnswerRecordRepository;
import com.yuan.exam.repository.ExamRecordRepository;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import com.yuan.exam.repository.QuestionSnapshotRepository;
import com.yuan.exam.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 考试管理 Service
 */
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final QuestionSnapshotRepository questionSnapshotRepository;
    private final UserRepository userRepository;

    public ExamService(ExamRepository examRepository,
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

    @Transactional(readOnly = true)
    public Result<PageResult<ExamVo>> list(Integer page, Integer size) {
        User user = currentUser();
        Pageable pageable = PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Exam> examPage = examRepository.findAll(pageable);
        List<ExamVo> list = examPage.getContent().stream()
                .map(e -> toVo(e, user))
                .toList();
        return Result.success(PageResult.of(list, examPage.getTotalElements(),
                pageable.getPageNumber() + 1, pageable.getPageSize()));
    }

    @Transactional(readOnly = true)
    public Result<ExamVo> get(Long id) {
        Optional<Exam> opt = examRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        return Result.success(toVo(opt.get(), currentUser()));
    }

    public Result<ExamVo> create(ExamVo vo) {
        Exam exam = new Exam();
        apply(exam, vo);
        exam = examRepository.save(exam);
        return Result.success(toVo(exam, currentUser()));
    }

    public Result<ExamVo> update(Long id, ExamVo vo) {
        Optional<Exam> opt = examRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        Exam exam = opt.get();
        apply(exam, vo);
        exam = examRepository.save(exam);
        return Result.success(toVo(exam, currentUser()));
    }

    /**
     * 删除考试：先清快照、作答、题目，再删考试
     */
    @Transactional
    public Result<Void> delete(Long id) {
        if (!examRepository.existsById(id)) {
            return Result.error(404, "考试不存在");
        }
        questionSnapshotRepository.deleteByExamRecord_Exam_Id(id);
        answerRecordRepository.deleteByExamRecord_Exam_Id(id);
        examRecordRepository.deleteByExam_Id(id);
        questionRepository.deleteByExam_Id(id);
        examRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 教师预览试卷（不计分、不建记录）
     */
    @Transactional(readOnly = true)
    public Result<List<QuestionVo>> preview(Long examId) {
        if (!examRepository.existsById(examId)) {
            return Result.error(404, "考试不存在");
        }
        List<QuestionVo> list = questionRepository.findByExam_Id(examId).stream()
                .map(q -> QuestionService.toVo(q, true))
                .toList();
        return Result.success(list);
    }

    /**
     * 按题目分之和回写考试总分
     */
    @Transactional
    public void syncTotalScore(Long examId) {
        examRepository.findById(examId).ifPresent(exam -> {
            int sum = questionRepository.findByExam_Id(examId).stream()
                    .mapToInt(q -> q.getScore() == null ? 0 : q.getScore())
                    .sum();
            exam.setTotalScore(sum);
            examRepository.save(exam);
        });
    }

    private void apply(Exam exam, ExamVo vo) {
        exam.setName(vo.getName());
        exam.setDuration(vo.getDuration());
        exam.setTotalScore(vo.getTotalScore());
        exam.setStartTime(vo.getStartTime());
        exam.setEndTime(vo.getEndTime());
    }

    private ExamVo toVo(Exam exam, User user) {
        ExamVo vo = new ExamVo();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setDuration(exam.getDuration());
        vo.setTotalScore(exam.getTotalScore());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setWindowStatus(resolveWindowStatus(exam, LocalDateTime.now()));
        vo.setFinished(false);
        vo.setInProgress(false);
        if (user != null) {
            List<ExamRecord> records = examRecordRepository.findByExam_IdAndUser_Id(exam.getId(), user.getId());
            if (!records.isEmpty()) {
                ExamRecord latest = records.get(0);
                if (latest.getStatus() == ExamStatus.IN_PROGRESS) {
                    vo.setInProgress(true);
                } else {
                    vo.setFinished(true);
                }
            }
        }
        return vo;
    }

    static String resolveWindowStatus(Exam exam, LocalDateTime now) {
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            return "NOT_STARTED";
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            return "CLOSED";
        }
        return "OPEN";
    }

    private User currentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username);
    }
}
