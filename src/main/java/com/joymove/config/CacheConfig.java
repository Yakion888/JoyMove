package com.joymove.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Redis 缓存配置。
 *
 * <p>为什么不加 Caffeine 多级缓存：Spring 的 {@code CompositeCacheManager}
 * 不是真正的 L1→L2 级联——它的 {@code getCache(name)} 只返回第一个匹配的缓存，
 * 所有读写只走 L1 Caffeine，Redis 永远不会被写入。
 * 真正的多级缓存需要自定义 {@code Cache} 实现，对当前项目规模属于过度设计。
 * 在面试中可以说："我评估过 CompositeCacheManager 的行为，发现它不做级联，
 * 真正的多级缓存需要用 Caffeine 包装 Redis 的自定义实现。"
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("sportProject", eternalCacheConfig());
        configs.put("medal", eternalCacheConfig());
        configs.put("userStats", redisCacheConfig(Duration.ofMinutes(10)));
        configs.put("nullMarker", redisCacheConfig(Duration.ofMinutes(1)));
        configs.put("aiReport", eternalCacheConfig());

        log.info("Redis 缓存生效: sportProject/medal/aiReport=永不过期, userStats=10min, nullMarker=1min");
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(configs)
                .cacheDefaults(redisCacheConfig(Duration.ofMinutes(5)))
                .build();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e,
                    org.springframework.cache.Cache cache, Object key) {
                log.warn("缓存 GET 失败 cache={} key={}, 回源: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCachePutError(RuntimeException e,
                    org.springframework.cache.Cache cache, Object key, Object value) {
                log.warn("缓存 PUT 失败 cache={} key={}: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheEvictError(RuntimeException e,
                    org.springframework.cache.Cache cache, Object key) {
                log.warn("缓存 EVICT 失败 cache={} key={}: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheClearError(RuntimeException e,
                    org.springframework.cache.Cache cache) {
                log.warn("缓存 CLEAR 失败 cache={}: {}", cache.getName(), e.getMessage());
            }
        };
    }

    private RedisCacheConfiguration eternalCacheConfig() {
        return redisCacheConfig(null);
    }

    private RedisCacheConfiguration redisCacheConfig(Duration ttl) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer))
                .disableCachingNullValues();

        if (ttl == null) return config;

        long baseSeconds = ttl.getSeconds();
        long jitter = ThreadLocalRandom.current()
                .nextLong(-baseSeconds / 7, baseSeconds / 7 + 1);
        return config.entryTtl(ttl.plusSeconds(jitter));
    }
}
