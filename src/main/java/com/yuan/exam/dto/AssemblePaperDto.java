package com.yuan.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 从题库组卷请求
 */
@Data
public class AssemblePaperDto {
    private List<Long> bankQuestionIds;
}
