package com.yuan.exam.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * - 認證與健康檢查公開放行
 * - 其餘路徑需登入（JWT）
 * - 細粒度角色限制透過 @PreAuthorize 在 Controller 上控制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF（前後端分離 + JWT 場景）
                .csrf(csrf -> csrf.disable())
                // 不使用 Session，改以 JWT 無狀態認證
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 認證與健康檢查接口公開
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()
                        // 其餘路徑一律需登入
                        .anyRequest().authenticated()
                )
                // 認證失敗（未登入 / Token 無效）與授權失敗（權限不足）回傳 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "未登入或 Token 無效"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, 403, "權限不足"))
                )
                // 在帳密認證過濾器之前掛上 JWT 過濾器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 直接寫出 JSON 錯誤回應（避免依賴 ObjectMapper）
     */
    private void writeJsonError(HttpServletResponse response, int httpStatus, int code, String msg) throws java.io.IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // 簡易 JSON：{"code":xxx,"msg":"xxx","data":null}
        String json = "{\"code\":" + code + ",\"msg\":\"" + msg.replace("\"", "\\\"") + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}


