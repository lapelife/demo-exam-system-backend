package com.yuan.exam.common;

import lombok.Data;

/**
 * 統一返回格式
 *
 * @param <T> 資料型別
 */
@Data
public class Result<T> {

    /** 狀態碼 */
    private int code;

    /** 提示訊息 */
    private String msg;

    /** 資料本體 */
    private T data;

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功回應（無資料）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功回應（帶資料）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 失敗回應
     *
     * @param code 狀態碼
     * @param msg  錯誤訊息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
