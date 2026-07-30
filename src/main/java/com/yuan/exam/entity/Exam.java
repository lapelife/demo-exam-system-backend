package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 考試實體
 */
@Data
@Entity
public class Exam {

    /** 主鍵 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 考試名稱 */
    private String name;

    /** 考試時長（分鐘） */
    private Integer duration;

    /** 考試總分 */
    private Integer totalScore;

    /** 開始時間 */
    private LocalDateTime startTime;

    /** 結束時間 */
    private LocalDateTime endTime;

    /** 建立時間 */
    @CreationTimestamp
    private LocalDateTime createTime;
}
