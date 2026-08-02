package com.yuan.exam.service;

import com.yuan.exam.common.PageRequests;
import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.common.SecurityUtils;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 题目管理 Service
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamService examService;

    public QuestionService(QuestionRepository questionRepository,
                           ExamRepository examRepository,
                           @Lazy ExamService examService) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.examService = examService;
    }

    @Transactional(readOnly = true)
    public Result<PageResult<QuestionVo>> listByExam(Long examId, Integer page, Integer size) {
        if (!examRepository.existsById(examId)) {
            return Result.error(404, "考试不存在");
        }
        boolean includeAnswer = SecurityUtils.isStaff();
        Pageable pageable = PageRequests.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Question> questionPage = questionRepository.findByExam_Id(examId, pageable);
        return Result.success(PageResult.of(
                questionPage.getContent().stream().map(q -> toVo(q, includeAnswer)).toList(),
                questionPage.getTotalElements(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize()));
    }

    @Transactional
    public Result<QuestionVo> create(Long examId, QuestionVo vo) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        Question q = new Question();
        q.setExam(examOpt.get());
        apply(q, vo);
        q = questionRepository.save(q);
        examService.syncTotalScore(examId);
        return Result.success(toVo(q, true));
    }

    @Transactional
    public Result<QuestionVo> update(Long questionId, QuestionVo vo) {
        Optional<Question> opt = questionRepository.findById(questionId);
        if (opt.isEmpty()) {
            return Result.error(404, "题目不存在");
        }
        Question q = opt.get();
        apply(q, vo);
        q = questionRepository.save(q);
        examService.syncTotalScore(q.getExam().getId());
        return Result.success(toVo(q, true));
    }

    @Transactional
    public Result<Void> delete(Long questionId) {
        Optional<Question> opt = questionRepository.findById(questionId);
        if (opt.isEmpty()) {
            return Result.error(404, "题目不存在");
        }
        Long examId = opt.get().getExam().getId();
        questionRepository.deleteById(questionId);
        examService.syncTotalScore(examId);
        return Result.success();
    }

    private void apply(Question q, QuestionVo vo) {
        q.setType(vo.getType());
        q.setContent(vo.getContent());
        q.setOptions(vo.getOptions());
        q.setAnswer(vo.getAnswer());
        q.setScore(vo.getScore());
    }

    public static QuestionVo toVo(Question q, boolean includeAnswer) {
        QuestionVo vo = new QuestionVo();
        vo.setId(q.getId());
        vo.setExamId(q.getExam().getId());
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(includeAnswer ? q.getAnswer() : null);
        vo.setScore(q.getScore());
        return vo;
    }
}
