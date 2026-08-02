package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BankSimilarQuestionVo {

    private Long id;
    private QuestionType type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String tag;
    private LocalDateTime createTime;

    /** 与组内锚点题的相似度 */
    private double similarity;

    /** 服务端建议保留（通常为组内最早入库） */
    private boolean suggestedKeep;
}
