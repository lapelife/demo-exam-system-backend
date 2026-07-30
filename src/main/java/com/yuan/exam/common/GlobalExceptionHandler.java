package com.yuan.exam.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局異常處理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理所有未捕獲的 Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系統異常：", e);
        return Result.error(500, "系統異常：" + e.getMessage());
    }

    /**
     * 處理 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("執行時期異常：", e);
        return Result.error(500, "執行時期異常：" + e.getMessage());
    }

    /**
     * 處理參數校驗失敗（@Valid 觸發）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 收集所有欄位的錯誤訊息
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("參數校驗失敗：{}", message);
        return Result.error(400, "參數校驗失敗：" + message);
    }

    /**
     * 處理 @PreAuthorize 角色不足（Spring Security 6 丟出 AuthorizationDeniedException）
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAuthorizationDenied(AuthorizationDeniedException e) {
        log.warn("權限不足：{}", e.getMessage());
        return Result.error(403, "權限不足");
    }

    /**
     * 處理一般存取被拒
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("存取被拒：{}", e.getMessage());
        return Result.error(403, "權限不足");
    }
}
