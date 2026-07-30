package com.yuan.exam.dto;

import com.yuan.exam.entity.ExamStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试作答记录视图对象
 */
@Data
public class ExamRecordVo {
    private Long id;
    private Long examId;
    private String examName;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer totalScore;
    private Integer examMaxScore;
    private ExamStatus status;
}
