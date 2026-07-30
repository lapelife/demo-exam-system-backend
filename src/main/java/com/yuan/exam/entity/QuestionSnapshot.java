package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 * 开考题目快照：锁定题干与答案，避免教师改题影响在考学生
 */
@Data
@Entity
public class QuestionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_record_id", nullable = false)
    private ExamRecord examRecord;

    /** 来源题目 ID（仅追溯，判分以快照为准） */
    private Long sourceQuestionId;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String content;

    private String options;

    private String answer;

    private Integer score;
}
