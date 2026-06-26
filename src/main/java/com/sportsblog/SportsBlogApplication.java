package com.sportsblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 悦动宝 JoyMove — 亲子运动陪伴平台
 */
@SpringBootApplication
@MapperScan("com.sportsblog.mapper")
public class SportsBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsBlogApplication.class, args);
    }
}
