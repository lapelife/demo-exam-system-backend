package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDedupGroupDecision {

    /** 本组要保留的题目 ID（至少一个） */
    private List<Long> keepIds = new ArrayList<>();

    /** 本组要删除的题目 ID */
    private List<Long> removeIds = new ArrayList<>();
}
