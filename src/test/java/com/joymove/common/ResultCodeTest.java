package com.joymove.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResultCode 状态码枚举单元测试
 */
class ResultCodeTest {

    @Test
    void success_shouldBe200() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
    }

    @Test
    void badRequest_shouldBe400() {
        assertEquals(400, ResultCode.BAD_REQUEST.getCode());
    }

    @Test
    void unauthorized_shouldBe401() {
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
    }

    @Test
    void forbidden_shouldBe403() {
        assertEquals(403, ResultCode.FORBIDDEN.getCode());
    }

    @Test
    void notFound_shouldBe404() {
        assertEquals(404, ResultCode.NOT_FOUND.getCode());
    }

    @Test
    void conflict_shouldBe409() {
        assertEquals(409, ResultCode.CONFLICT.getCode());
    }

    @Test
    void error_shouldBe500() {
        assertEquals(500, ResultCode.ERROR.getCode());
    }

    @Test
    void enumCount_shouldBe9() {
        assertEquals(9, ResultCode.values().length);
    }
}
