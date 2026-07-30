package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.dto.StartExamVo;
import com.yuan.exam.dto.SubmitAnswerDto;
import com.yuan.exam.service.ExamTakingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生作答接口：开始作答、提交并自动判分（仅 STUDENT）
 */
@RestController
@RequestMapping("/api/take")
@PreAuthorize("hasRole('STUDENT')")
public class ExamTakingController {

    private final ExamTakingService examTakingService;

    public ExamTakingController(ExamTakingService examTakingService) {
        this.examTakingService = examTakingService;
    }

    /**
     * 开始作答：建立作答记录并返回题目（不含答案）
     */
    @PostMapping("/start/{examId}")
    public Result<StartExamVo> start(@PathVariable Long examId) {
        return examTakingService.startExam(examId);
    }

    /**
     * 提交作答并自动判分
     */
    @PostMapping("/submit")
    public Result<ScoreVo> submit(@Valid @RequestBody SubmitAnswerDto dto) {
        return examTakingService.submitExam(dto);
    }
}
