package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 单题作答记录实体（每题一笔，含判分结果；进行中可为草稿）
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"exam_record_id", "question_id"}))
public class AnswerRecord {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属考试作答记录 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_record_id", nullable = false)
    private ExamRecord examRecord;

    /** 题目 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** 学生作答内容（单选/判断为单值；多选为逗号分隔） */
    private String answer;

    /** 是否答对（草稿阶段可为 null） */
    private Boolean isCorrect;

    /** 本题得分（草稿阶段可为 null） */
    private Integer score;
}
