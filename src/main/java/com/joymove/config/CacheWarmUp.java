package com.joymove.config;

import com.joymove.service.MedalService;
import com.joymove.service.SportProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 缓存预热：应用启动时主动加载热点数据到 Redis，
 * 避免第一个用户吃冷启动延迟。
 */
@Slf4j
@Component
public class CacheWarmUp implements CommandLineRunner {

    @Autowired
    private SportProjectService sportProjectService;

    @Autowired
    private MedalService medalService;

    @Override
    public void run(String... args) {
        log.info("缓存预热开始...");
        long start = System.currentTimeMillis();

        sportProjectService.getAllEnabled();  // → 写入 sportProject::enabled
        sportProjectService.getAll();         // → 写入 sportProject::all
        medalService.getAll();                // → 写入 medal::all

        log.info("缓存预热完成，耗时 {}ms", System.currentTimeMillis() - start);
    }
}
