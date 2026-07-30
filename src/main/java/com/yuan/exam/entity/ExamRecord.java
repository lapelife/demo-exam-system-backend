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
 * 考試作答記錄實體（一次完整作答對應一筆）
 */
@Data
@Entity
public class ExamRecord {

    /** 主鍵 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所屬考試 ID */
    private Long examId;

    /** 作答學生 ID */
    private Long userId;

    /** 開始作答時間 */
    private LocalDateTime startTime;

    /** 提交時間 */
    private LocalDateTime submitTime;

    /** 本次作答總得分 */
    private Integer totalScore;

    /** 作答狀態 */
    @Enumerated(EnumType.STRING)
    private ExamStatus status;
}
