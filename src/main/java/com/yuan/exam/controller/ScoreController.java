package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ExamRecordVo;
import com.yuan.exam.dto.ExamStatsVo;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.service.ScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成绩查询接口（具体路径写在变量路径之前）
 */
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping
    public Result<List<ExamRecordVo>> myScores() {
        return scoreService.myScores();
    }

    @GetMapping("/exam/{examId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<ExamStatsVo> examStats(@PathVariable Long examId) {
        return scoreService.examStats(examId);
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<ExamRecordVo>> examScores(@PathVariable Long examId) {
        return scoreService.examScores(examId);
    }

    @GetMapping("/{examRecordId}")
    public Result<ScoreVo> detail(@PathVariable Long examRecordId) {
        return scoreService.detail(examRecordId);
    }
}
