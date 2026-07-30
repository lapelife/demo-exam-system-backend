package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import com.yuan.exam.dto.ExamRecordVo;
import com.yuan.exam.dto.ScoreVo;
import com.yuan.exam.service.ScoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成绩查询接口（学生查看自己的成绩与明细）
 */
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /**
     * 我的成绩列表
     */
    @GetMapping
    public Result<List<ExamRecordVo>> myScores() {
        return scoreService.myScores();
    }

    /**
     * 单次作答成绩明细
     */
    @GetMapping("/{examRecordId}")
    public Result<ScoreVo> detail(@PathVariable Long examRecordId) {
        return scoreService.detail(examRecordId);
    }
}
