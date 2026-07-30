package com.yuan.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交作答請求
 */
@Data
public class SubmitAnswerDto {
    @NotNull
    private Long examRecordId;
    @NotNull
    private List<AnswerDto> answers;
}
