package com.yuan.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 学生开始作答时返回的对象：包含作答记录 ID、考试信息、题目清单
 */
@Data
public class StartExamVo {
    private Long examRecordId;
    private ExamVo exam;
    /** 题目清单（不含 answer） */
    private List<QuestionVo> questions;
}
