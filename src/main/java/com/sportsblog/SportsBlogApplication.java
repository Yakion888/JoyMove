package com.sportsblog;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 悦动宝 JoyMove — 亲子运动陪伴平台
 */
@SpringBootApplication
@MapperScan("com.sportsblog.mapper")
public class SportsBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportsBlogApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("悦动宝 JoyMove API")
                        .version("1.0.0")
                        .description("亲子运动陪伴平台接口文档"));
    }
}
