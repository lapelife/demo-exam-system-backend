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
 * - 认证与健康检查公开放行
 * - 其余路径需登录（JWT）
 * - 细粒度角色限制透过 @PreAuthorize 在 Controller 上控制
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
                // 关闭 CSRF（前后端分离 + JWT 场景）
                .csrf(csrf -> csrf.disable())
                // 不使用 Session，改以 JWT 无状态认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 认证与健康检查接口公开
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()
                        // 其余路径一律需登录
                        .anyRequest().authenticated()
                )
                // 认证失败（未登录 / Token 无效）与授权失败（权限不足）返回 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "未登录或 Token 无效"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, 403, "权限不足"))
                )
                // 在账密认证过滤器之前挂上 JWT 过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 直接写出 JSON 错误响应（避免依赖 ObjectMapper）
     */
    private void writeJsonError(HttpServletResponse response, int httpStatus, int code, String msg) throws java.io.IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // 简易 JSON：{"code":xxx,"msg":"xxx","data":null}
        String json = "{\"code\":" + code + ",\"msg\":\"" + msg.replace("\"", "\\\"") + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}


