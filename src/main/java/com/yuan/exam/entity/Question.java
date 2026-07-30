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
 * 题目实体
 */
@Data
@Entity
public class Question {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属考试 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** 题目类型 */
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    /** 题目内容 */
    private String content;

    /** 选项（JSON 字符串） */
    private String options;

    /** 正确答案 */
    private String answer;

    /** 分值 */
    private Integer score;
}
