package com.joymove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池 — 用于不需要同步等待结果的后台任务。
 *
 * <h3>当前用途</h3>
 * <ul>
 *   <li>通知发送 — 打卡/审核后触发，用户不关心通知何时入库
 *       {@link com.joymove.service.impl.NotificationServiceImpl#create}</li>
 *   <li>勋章检查 — 打卡后触发，涉及多条查询，不拖慢打卡接口
 *       {@link com.joymove.service.impl.MedalServiceImpl#checkAndAward}</li>
 * </ul>
 *
 * <h3>后续扩展</h3>
 * AI 调用（DeepSeek API 耗时 5-10s）可加 {@code @Async}，但前端需配合轮询/WebSocket 才能拿到结果。
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = cores * 2;
        int maxPoolSize = corePoolSize * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("joymove-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
