package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDedupApplyRequest {

    private List<BankDedupGroupDecision> groups = new ArrayList<>();
}
