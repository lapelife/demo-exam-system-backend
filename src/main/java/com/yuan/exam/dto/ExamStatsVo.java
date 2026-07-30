package com.yuan.exam.dto;

import lombok.Data;

/**
 * 考试成绩统计
 */
@Data
public class ExamStatsVo {
    private Long examId;
    private String examName;
    private long participantCount;
    private long gradedCount;
    private Double averageScore;
    private Integer maxScore;
    private Integer minScore;
    private Integer examMaxScore;
}
