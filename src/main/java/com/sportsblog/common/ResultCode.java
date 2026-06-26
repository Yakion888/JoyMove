package com.sportsblog.common;

/**
 * 统一返回状态码枚举
 */
public enum ResultCode {

    /** 请求成功 */
    SUCCESS(200, "操作成功"),
    /** 服务器内部错误 */
    ERROR(500, "服务器内部错误"),
    /** 未授权/未登录 */
    UNAUTHORIZED(401, "未授权，请先登录"),
    /** 权限不足 */
    FORBIDDEN(403, "权限不足，无法访问");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
