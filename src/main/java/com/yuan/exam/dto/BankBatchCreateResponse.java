package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankBatchCreateResponse {

    private List<BankQuestionVo> saved = new ArrayList<>();
    private int savedCount;
    private int skippedCount;
}
