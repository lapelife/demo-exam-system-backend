package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

/**
 * 单题判分明细（成绩查询时返回）
 */
@Data
public class AnswerDetailVo {
    private Long questionId;
    private String content;
    private QuestionType type;
    private String options;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer score;
    private Integer maxScore;
}
