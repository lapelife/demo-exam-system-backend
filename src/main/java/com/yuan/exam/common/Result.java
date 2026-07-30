package com.yuan.exam.common;

import lombok.Data;

/**
 * 统一返回格式
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {

    /** 状态码 */
    private int code;

    /** 提示消息 */
    private String msg;

    /** 数据本体 */
    private T data;

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 失败响应
     *
     * @param code 状态码
     * @param msg  错误消息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
