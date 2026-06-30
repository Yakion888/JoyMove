package com.joymove.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 缓存配置 — 三级缓存，不同 TTL。
 *
 * <ul>
 *   <li>sportProject — 24h，运动项目，管理员改项目时主动清</li>
 *   <li>medal — 24h，勋章定义，几乎不变</li>
 *   <li>userStats — 10min，用户公开主页统计，打卡后主动清</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerBuilderCustomizer() {
        return builder -> {
            builder.withCacheConfiguration("sportProject",
                    redisCacheConfig(Duration.ofHours(24)));
            builder.withCacheConfiguration("medal",
                    redisCacheConfig(Duration.ofHours(24)));
            builder.withCacheConfiguration("userStats",
                    redisCacheConfig(Duration.ofMinutes(10)));
        };
    }

    /**
     * 通用配置：Jackson JSON 序列化，支持带类型的对象反序列化
     */
    private RedisCacheConfiguration redisCacheConfig(Duration ttl) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer))
                .disableCachingNullValues();
    }
}
