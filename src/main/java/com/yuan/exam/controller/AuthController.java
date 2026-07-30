package com.yuan.exam.controller;

import com.yuan.exam.common.JwtUtils;
import com.yuan.exam.common.Result;
import com.yuan.exam.dto.LoginRequest;
import com.yuan.exam.dto.LoginResponse;
import com.yuan.exam.entity.User;
import com.yuan.exam.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 認證相關接口：登入、取得當前使用者資訊
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    /**
     * 使用者登入
     *
     * @param request 登入請求（username、password）
     * @return 成功時回傳 Token 與使用者資訊
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 依使用者名稱查詢
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error(401, "用戶不存在");
        }

        // 密碼比對（目前為明文比對；上線應改為 BCrypt 加密比對）
        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error(401, "密碼錯誤");
        }

        // 產生 JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getRole().name());
        return Result.success(response);
    }

    /**
     * 取得當前登入使用者資訊
     * 從 Header Authorization 讀取 Bearer Token 並解析
     *
     * @param request HTTP 請求
     * @return 使用者資訊
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "未提供有效的 Token");
        }

        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(401, "Token 無效或已過期");
        }

        String username = jwtUtils.parseToken(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Result.error(401, "用戶不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("username", user.getUsername());
        info.put("role", user.getRole().name());
        info.put("nickname", user.getNickname());
        info.put("email", user.getEmail());
        return Result.success(info);
    }
}
