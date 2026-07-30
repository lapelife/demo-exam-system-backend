package com.yuan.exam.entity;

/**
 * 考試作答狀態枚舉
 */
public enum ExamStatus {
    /** 進行中（已開始尚未提交） */
    IN_PROGRESS,
    /** 已提交（待判分或已判分） */
    SUBMITTED,
    /** 已判分完成 */
    GRADED
}
