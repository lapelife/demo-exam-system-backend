package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.AssemblePaperDto;
import com.yuan.exam.dto.ExamVo;
import com.yuan.exam.dto.QuestionVo;
import com.yuan.exam.service.BankQuestionService;
import com.yuan.exam.service.ExamService;
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
 * 考试管理接口
 */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;
    private final BankQuestionService bankQuestionService;

    public ExamController(ExamService examService, BankQuestionService bankQuestionService) {
        this.examService = examService;
        this.bankQuestionService = bankQuestionService;
    }

    @GetMapping
    public Result<List<ExamVo>> list() {
        return examService.list();
    }

    @GetMapping("/{id}")
    public Result<ExamVo> get(@PathVariable Long id) {
        return examService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<ExamVo> create(@RequestBody ExamVo vo) {
        return examService.create(vo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<ExamVo> update(@PathVariable Long id, @RequestBody ExamVo vo) {
        return examService.update(id, vo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Void> delete(@PathVariable Long id) {
        return examService.delete(id);
    }

    /** 教师预览试卷（含答案，不计分） */
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<QuestionVo>> preview(@PathVariable Long id) {
        return examService.preview(id);
    }

    /** 从题库组卷：复制题目到本场考试 */
    @PostMapping("/{id}/assemble")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<QuestionVo>> assemble(@PathVariable Long id, @RequestBody AssemblePaperDto dto) {
        return bankQuestionService.assemble(id, dto);
    }
}
