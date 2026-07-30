package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

/**
 * 题目视图对象
 * 学生作答时 answer 字段会被清空，避免泄漏答案
 */
@Data
public class QuestionVo {
    private Long id;
    private Long examId;
    private QuestionType type;
    private String content;
    private String options;
    /** 正确答案；学生视图时为 null */
    private String answer;
    private Integer score;
}
