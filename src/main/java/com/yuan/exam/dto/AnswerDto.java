package com.yuan.exam.dto;

import lombok.Data;

/**
 * 單題作答請求
 */
@Data
public class AnswerDto {
    private Long questionId;
    /** 學生作答內容（單選/判斷為單值；多選為逗號分隔） */
    private String answer;
}
