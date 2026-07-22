package com.yuan.exam.controller;

import com.yuan.exam.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康檢查接口
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康檢查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
