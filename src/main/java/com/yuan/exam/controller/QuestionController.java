package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.service.QuestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 題目管理接口（依考試 ID）
 * - GET：所有登入者皆可（學生作答時取題）
 * - POST/PUT/DELETE：限 ADMIN/TEACHER
 */
@RestController
@RequestMapping("/api/exams/{examId}/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public Result<List<QuestionVo>> list(@PathVariable Long examId) {
        return questionService.listByExam(examId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<QuestionVo> create(@PathVariable Long examId, @RequestBody QuestionVo vo) {
        return questionService.create(examId, vo);
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<QuestionVo> update(@PathVariable Long questionId, @RequestBody QuestionVo vo) {
        return questionService.update(questionId, vo);
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Void> delete(@PathVariable Long questionId) {
        return questionService.delete(questionId);
    }
}
