package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 题库题目（独立于考试，可组卷复制到 Exam）
 */
@Data
@Entity
public class BankQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String content;

    /** 选项 JSON 字符串 */
    private String options;

    private String answer;

    private Integer score;

    /** 可选标签，便于筛选 */
    private String tag;

    @CreationTimestamp
    private LocalDateTime createTime;
}
