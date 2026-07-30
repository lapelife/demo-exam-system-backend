package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试作答记录实体（一次完整作答对应一笔）
 */
@Data
@Entity
public class ExamRecord {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属考试 ID */
    private Long examId;

    /** 作答学生 ID */
    private Long userId;

    /** 开始作答时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 本次作答总得分 */
    private Integer totalScore;

    /** 作答状态 */
    @Enumerated(EnumType.STRING)
    private ExamStatus status;
}
