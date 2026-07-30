package com.yuan.exam.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：取得當前登入使用者資訊
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 取得當前登入使用者名稱（JWT principal）
     *
     * @return 使用者名稱；未登入則回 null
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
