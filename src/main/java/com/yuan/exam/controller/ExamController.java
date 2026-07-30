package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ExamVo;
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
 * 考試管理接口
 * - GET 列表/詳情：所有登入者皆可（學生用於查看可參加考試）
 * - POST/PUT/DELETE：限 ADMIN/TEACHER
 */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
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
}
