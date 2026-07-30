package com.yuan.exam.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生开始作答时返回的对象
 */
@Data
public class StartExamVo {
    private Long examRecordId;
    private ExamVo exam;
    /** 题目清单（不含 answer） */
    private List<QuestionVo> questions;
    /** 服务端权威剩余秒数 */
    private Long remainSeconds;
    /** 截止绝对时间 */
    private LocalDateTime deadline;
    /** 已保存草稿：questionId -> answer */
    private List<AnswerDto> savedAnswers;
}
