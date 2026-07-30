package com.yuan.exam.dto;

import com.yuan.exam.entity.ExamStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成績視圖物件（含每題判分明細）
 */
@Data
public class ScoreVo {
    private Long examRecordId;
    private Long examId;
    private String examName;
    private Integer totalScore;
    private Integer examMaxScore;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private ExamStatus status;
    /** 每題判分明細 */
    private List<AnswerDetailVo> details;
}
