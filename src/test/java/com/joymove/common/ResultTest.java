package com.joymove.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 统一响应类单元测试
 */
class ResultTest {

    @Test
    void success_shouldReturnCode200() {
        Result<String> result = Result.success("hello");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals("hello", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void success_withoutData_shouldReturnCode200() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void error_withMessage_shouldReturnCode500() {
        Result<Void> result = Result.error("出错了");

        assertEquals(500, result.getCode());
        assertEquals("出错了", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    void error_withResultCode_shouldUseEnumValues() {
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);

        assertEquals(401, result.getCode());
        assertEquals("未授权，请先登录", result.getMsg());
    }

    @Test
    void error_withCustomCodeAndMessage_shouldUseThem() {
        Result<Void> result = Result.error(409, "用户名已存在");

        assertEquals(409, result.getCode());
        assertEquals("用户名已存在", result.getMsg());
    }
}
