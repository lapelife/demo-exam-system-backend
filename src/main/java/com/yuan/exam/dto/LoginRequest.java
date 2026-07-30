package com.yuan.exam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    /** 用户名称 */
    @NotBlank(message = "不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "不能为空")
    private String password;
}
