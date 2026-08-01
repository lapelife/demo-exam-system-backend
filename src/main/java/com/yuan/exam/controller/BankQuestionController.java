package com.yuan.exam.controller;

import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.BankQuestionVo;
import com.yuan.exam.service.BankQuestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题库管理（ADMIN/TEACHER）
 */
@RestController
@RequestMapping("/api/bank/questions")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class BankQuestionController {

    private final BankQuestionService bankQuestionService;

    public BankQuestionController(BankQuestionService bankQuestionService) {
        this.bankQuestionService = bankQuestionService;
    }

    @GetMapping
    public Result<PageResult<BankQuestionVo>> list(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return bankQuestionService.list(tag, page, size);
    }

    @PostMapping
    public Result<BankQuestionVo> create(@RequestBody BankQuestionVo vo) {
        return bankQuestionService.create(vo);
    }

    @PutMapping("/{id}")
    public Result<BankQuestionVo> update(@PathVariable Long id, @RequestBody BankQuestionVo vo) {
        return bankQuestionService.update(id, vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return bankQuestionService.delete(id);
    }
}
