package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

/**
 * 題目視圖物件
 * 學生作答時 answer 欄位會被清空，避免洩漏答案
 */
@Data
public class QuestionVo {
    private Long id;
    private Long examId;
    private QuestionType type;
    private String content;
    private String options;
    /** 正確答案；學生視圖時為 null */
    private String answer;
    private Integer score;
}
