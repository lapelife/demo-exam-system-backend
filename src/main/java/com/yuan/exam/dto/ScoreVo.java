package com.yuan.exam.dto;

import com.yuan.exam.entity.ExamStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩视图对象（含每题判分明细）
 */
@Data
public class ScoreVo {
    private Long examRecordId;
    private Long examId;
    private String examName;
    private Integer totalScore;
    private Integer examMaxScore;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private ExamStatus status;
    /** 每题判分明细 */
    private List<AnswerDetailVo> details;
}
