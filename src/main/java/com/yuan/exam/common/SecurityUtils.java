package com.yuan.exam.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return principal.toString();
    }

    /**
     * 当前用户是否拥有任一角色（传入不含 ROLE_ 前缀，如 ADMIN）
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String a = authority.getAuthority();
            for (String role : roles) {
                if (("ROLE_" + role).equals(a) || role.equals(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否为教师或管理员（可见标准答案）
     */
    public static boolean isStaff() {
        return hasAnyRole("ADMIN", "TEACHER");
    }
}
