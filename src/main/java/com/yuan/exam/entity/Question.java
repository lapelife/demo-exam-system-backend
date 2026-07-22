package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * 題目實體
 */
@Data
@Entity
public class Question {

    /** 主鍵 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所屬考試 ID */
    private Long examId;

    /** 題目類型 */
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    /** 題目內容 */
    private String content;

    /** 選項（JSON 字串） */
    private String options;

    /** 正確答案 */
    private String answer;

    /** 分值 */
    private Integer score;
}
