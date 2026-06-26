package com.sportsblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SportsBlog 体育博客系统 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.sportsblog.mapper")
public class SportsBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsBlogApplication.class, args);
    }
}
