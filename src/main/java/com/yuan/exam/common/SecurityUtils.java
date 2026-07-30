package com.yuan.exam.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：取得当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 取得当前登录用户名称（JWT principal）
     *
     * @return 用户名称；未登录则回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal == null ? null : principal.toString();
    }
}
