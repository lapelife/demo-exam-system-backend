package com.yuan.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankDuplicateCheckRequest {

    private List<BankQuestionVo> items = new ArrayList<>();
}
