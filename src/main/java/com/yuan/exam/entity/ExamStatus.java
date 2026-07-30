package com.yuan.exam.entity;

/**
 * 考试作答状态枚举
 */
public enum ExamStatus {
    /** 进行中（已开始尚未提交） */
    IN_PROGRESS,
    /** 已判分完成（主动提交） */
    GRADED,
    /** 超时自动交卷并判分 */
    TIMEOUT_AUTO_GRADED
}
