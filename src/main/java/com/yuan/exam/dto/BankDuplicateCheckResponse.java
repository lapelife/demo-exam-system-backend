package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDuplicateCheckResponse {

    private List<BankDuplicateCheckItemResult> results = new ArrayList<>();
    private int duplicateCount;
}
