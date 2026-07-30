package com.yuan.exam.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 目前先全放行，JWT 過濾器已掛上，後續可再收緊權限
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF（前後端分離 + JWT 場景）
                .csrf(csrf -> csrf.disable())
                // 不使用 Session，改以 JWT 無狀態認證
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 明確放行認證與健康檢查接口
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()
                        // 其他路徑暫時一律放行（JWT 強制攔截下一步再加）
                        .anyRequest().permitAll()
                )
                // 在帳密認證過濾器之前掛上 JWT 過濾器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
