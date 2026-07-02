package com.joymove.config;

import com.joymove.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP 限流拦截器 — 滑动窗口计数器，单机版。
 *
 * <p>每个 IP 每秒最多 {@link #MAX_REQUESTS_PER_SECOND} 次请求，
 * 超限返回 429 Too Many Requests。
 * 生产环境应替换为 Redis 令牌桶，实现分布式限流。</p>
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_SECOND = 5;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = getClientIp(request);
        long now = System.currentTimeMillis();
        long windowStart = now / 1000; // 秒级窗口

        WindowCounter counter = counters.computeIfAbsent(ip, k -> new WindowCounter());
        synchronized (counter) {
            if (counter.windowStart != windowStart) {
                counter.windowStart = windowStart;
                counter.count.set(0);
            }
            if (counter.count.incrementAndGet() > MAX_REQUESTS_PER_SECOND) {
                log.warn("限流触发: IP={}, count={}", ip, counter.count.get());
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(mapper.writeValueAsString(
                        Result.error(429, "请求过于频繁，请稍后重试")));
                return false;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    private static class WindowCounter {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);
    }
}
