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
    /** NOT_STARTED / OPEN / CLOSED（相对当前时间） */
    private String windowStatus;
    /** 当前用户是否已完成该考试（学生视角） */
    private Boolean finished;
    /** 当前用户是否有进行中记录 */
    private Boolean inProgress;
}
