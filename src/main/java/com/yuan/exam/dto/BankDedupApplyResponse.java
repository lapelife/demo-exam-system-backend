package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDedupApplyResponse {

    private int deletedCount;
    private List<Long> deletedIds = new ArrayList<>();
}
