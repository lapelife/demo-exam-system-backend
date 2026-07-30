package com.yuan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT Token */
    private String token;

    /** 用户名称 */
    private String username;

    /** 角色 */
    private String role;
}
