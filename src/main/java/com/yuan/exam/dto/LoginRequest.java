package com.yuan.exam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求 DTO
 */
@Data
public class LoginRequest {

    /** 使用者名稱 */
    @NotBlank(message = "不能為空")
    private String username;

    /** 密碼 */
    @NotBlank(message = "不能為空")
    private String password;
}
