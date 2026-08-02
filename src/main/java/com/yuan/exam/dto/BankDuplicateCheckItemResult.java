package com.yuan.exam.dto;

import lombok.Data;

@Data
public class BankDuplicateCheckItemResult {

    private int index;
    private boolean duplicate;
    /** 题库中已存在题目的 id（若有） */
    private Long existingId;
    /** batch 内与更早条目重复 */
    private boolean duplicateInBatch;
}
