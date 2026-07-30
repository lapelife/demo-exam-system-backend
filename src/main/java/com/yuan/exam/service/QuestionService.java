package com.yuan.exam.service;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.entity.Exam;
import com.yuan.exam.entity.Question;
import com.yuan.exam.repository.ExamRepository;
import com.yuan.exam.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 题目管理 Service：负责题目的 CRUD（依考试 ID）
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public QuestionService(QuestionRepository questionRepository, ExamRepository examRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
    }

    /**
     * 列出某考试的所有题目
     */
    public Result<List<QuestionVo>> listByExam(Long examId) {
        if (!examRepository.existsById(examId)) {
            return Result.error(404, "考试不存在");
        }
        List<QuestionVo> list = questionRepository.findByExam_Id(examId).stream()
                .map(this::toVo)
                .toList();
        return Result.success(list);
    }

    /**
     * 新增题目
     */
    public Result<QuestionVo> create(Long examId, QuestionVo vo) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return Result.error(404, "考试不存在");
        }
        Question q = new Question();
        q.setExam(examOpt.get());
        q.setType(vo.getType());
        q.setContent(vo.getContent());
        q.setOptions(vo.getOptions());
        q.setAnswer(vo.getAnswer());
        q.setScore(vo.getScore());
        q = questionRepository.save(q);
        return Result.success(toVo(q));
    }

    /**
     * 更新题目
     */
    public Result<QuestionVo> update(Long questionId, QuestionVo vo) {
        Optional<Question> opt = questionRepository.findById(questionId);
        if (opt.isEmpty()) {
            return Result.error(404, "题目不存在");
        }
        Question q = opt.get();
        q.setType(vo.getType());
        q.setContent(vo.getContent());
        q.setOptions(vo.getOptions());
        q.setAnswer(vo.getAnswer());
        q.setScore(vo.getScore());
        q = questionRepository.save(q);
        return Result.success(toVo(q));
    }

    /**
     * 删除题目
     */
    public Result<Void> delete(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            return Result.error(404, "题目不存在");
        }
        questionRepository.deleteById(questionId);
        return Result.success();
    }

    private QuestionVo toVo(Question q) {
        QuestionVo vo = new QuestionVo();
        vo.setId(q.getId());
        vo.setExamId(q.getExam().getId());
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setScore(q.getScore());
        return vo;
    }
}
