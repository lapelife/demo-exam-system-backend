package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankBatchCreateRequest {

    private List<BankQuestionVo> items = new ArrayList<>();

    /** 为 true 时跳过与题库或批次内重复的题目，不报错 */
    private boolean skipDuplicates = true;
}
