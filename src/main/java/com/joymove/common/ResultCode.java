package com.joymove.common;

/**
 * 统一返回状态码枚举
 */
public enum ResultCode {

    /** 请求成功 */
    SUCCESS(200, "操作成功"),
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未授权/未登录 */
    UNAUTHORIZED(401, "未授权，请先登录"),
    /** 权限不足 */
    FORBIDDEN(403, "权限不足，无法访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    /** 数据冲突 */
    CONFLICT(409, "数据冲突，请刷新后重试"),
    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    /** 服务器内部错误 */
    ERROR(500, "服务器内部错误");

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
