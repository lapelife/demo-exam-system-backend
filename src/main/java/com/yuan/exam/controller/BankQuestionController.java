package com.yuan.exam.controller;

import com.yuan.exam.common.PageResult;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.BankBatchCreateRequest;
import com.yuan.exam.dto.BankBatchCreateResponse;
import com.yuan.exam.dto.BankDedupApplyRequest;
import com.yuan.exam.dto.BankDedupApplyResponse;
import com.yuan.exam.dto.BankDedupScanRequest;
import com.yuan.exam.dto.BankDedupScanResponse;
import com.yuan.exam.dto.BankDuplicateCheckRequest;
import com.yuan.exam.dto.BankDuplicateCheckResponse;
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

    /** 检查题目是否与题库或批次内重复 */
    @PostMapping("/check-duplicates")
    public Result<BankDuplicateCheckResponse> checkDuplicates(@RequestBody BankDuplicateCheckRequest request) {
        return bankQuestionService.checkDuplicates(request);
    }

    /** 批量入库，默认跳过重复题 */
    @PostMapping("/batch")
    public Result<BankBatchCreateResponse> batchCreate(@RequestBody BankBatchCreateRequest request) {
        return bankQuestionService.batchCreate(request);
    }

    /** 扫描题库内高度相似题目分组 */
    @PostMapping("/dedup/scan")
    public Result<BankDedupScanResponse> scanDuplicates(@RequestBody(required = false) BankDedupScanRequest request) {
        return bankQuestionService.scanDuplicates(request == null ? new BankDedupScanRequest() : request);
    }

    /** 按用户勾选结果删除未保留的重复题 */
    @PostMapping("/dedup/apply")
    public Result<BankDedupApplyResponse> applyDedup(@RequestBody BankDedupApplyRequest request) {
        return bankQuestionService.applyDedup(request);
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
