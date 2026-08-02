package com.yuan.exam.dto;

import com.yuan.exam.entity.QuestionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankSimilarGroupVo {

    private int groupIndex;
    private QuestionType type;
    /** 组内最高相似度 */
    private double maxSimilarity;
    private Long suggestedKeepId;
    private List<BankSimilarQuestionVo> questions = new ArrayList<>();
}
