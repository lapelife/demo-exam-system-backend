package com.yuan.exam.dto;

import lombok.Data;

@Data
public class BankDedupScanRequest {

    /** 按标签筛选；空则扫描全部题库 */
    private String tag;

    /**
     * 相似度阈值 0~1，默认 0.88。
     * exactOnly=true 时忽略该值，仅匹配规范化后完全相同的题干。
     */
    private Double threshold;

    /** 仅精确等价（规范化后完全相同），不做近似匹配 */
    private boolean exactOnly;
}
