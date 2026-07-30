package com.yuan.exam.entity;

/**
 * 考试作答状态枚举
 */
public enum ExamStatus {
    /** 进行中（已开始尚未提交） */
    IN_PROGRESS,
    /** 已提交（待判分或已判分） */
    SUBMITTED,
    /** 已判分完成 */
    GRADED
}
