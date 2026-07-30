package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 考试实体
 */
@Data
@Entity
public class Exam {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 考试名称 */
    private String name;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 考试总分 */
    private Integer totalScore;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 建立时间 */
    @CreationTimestamp
    private LocalDateTime createTime;
}
