package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDedupScanResponse {

    private int scannedCount;
    private int groupCount;
    private int duplicateQuestionCount;
    private double threshold;
    private boolean exactOnly;
    private List<BankSimilarGroupVo> groups = new ArrayList<>();
}
