package com.yuan.exam.dto;

import lombok.Data;

/**
 * 单题作答请求
 */
@Data
public class AnswerDto {
    private Long questionId;
    /** 学生作答内容（单选/判断为单值；多选为逗号分隔） */
    private String answer;
}
