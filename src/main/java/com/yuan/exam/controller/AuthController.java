package com.yuan.exam.controller;

import com.yuan.exam.common.JwtUtils;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.LoginRequest;
import com.yuan.exam.dto.LoginResponse;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证相关接口：登录、取得当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求（username、password）
     * @return 成功时返回 Token 与用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 依用户名称查询
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // BCrypt 密码比对
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "密码错误");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            return Result.error(403, "账号已禁用");
        }

        // 产生 JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getRole().name());
        return Result.success(response);
    }

    /**
     * 取得当前登录用户信息
     * 从 Header Authorization 读取 Bearer Token 并解析
     *
     * @param request HTTP 请求
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "未提供有效的 Token");
        }

        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(401, "Token 无效或已过期");
        }

        String username = jwtUtils.parseToken(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("username", user.getUsername());
        info.put("role", user.getRole().name());
        info.put("nickname", user.getNickname());
        info.put("email", user.getEmail());
        info.put("enabled", user.getEnabled() == null || user.getEnabled());
        return Result.success(info);
    }
}
