package com.yuan.exam.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考試視圖物件
 */
@Data
public class ExamVo {
    private Long id;
    private String name;
    private Integer duration;
    private Integer totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
