package com.yuan.exam.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试视图对象
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
