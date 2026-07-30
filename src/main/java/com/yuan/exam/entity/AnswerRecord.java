package com.yuan.exam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * 單題作答記錄實體（每題一筆，含判分結果）
 */
@Data
@Entity
public class AnswerRecord {

    /** 主鍵 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所屬考試作答記錄 ID */
    private Long examRecordId;

    /** 題目 ID */
    private Long questionId;

    /** 學生作答內容（單選/判斷為單值；多選為逗號分隔） */
    private String answer;

    /** 是否答對 */
    private Boolean isCorrect;

    /** 本題得分 */
    private Integer score;
}
