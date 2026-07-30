package com.yuan.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 學生開始作答時回傳的物件：包含作答記錄 ID、考試資訊、題目清單
 */
@Data
public class StartExamVo {
    private Long examRecordId;
    private ExamVo exam;
    /** 題目清單（不含 answer） */
    private List<QuestionVo> questions;
}
