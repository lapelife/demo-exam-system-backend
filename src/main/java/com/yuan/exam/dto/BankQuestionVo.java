package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BankQuestionVo {
    private Long id;
    private QuestionType type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String tag;
    private LocalDateTime createTime;
}
