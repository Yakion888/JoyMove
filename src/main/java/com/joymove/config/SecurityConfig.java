package com.joymove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置 — 悦动宝 JoyMove
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 角色层次：ADMIN > USER
     * 拥有 ADMIN 角色的用户自动继承 USER 角色的所有权限
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    /**
     * 安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 放行公开资源
            .authorizeRequests()
                // 注册登录
                .antMatchers("/register", "/login", "/doLogin", "/api/auth/register").permitAll()
                // 静态资源
                .antMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // 首页 & 搜索
                .antMatchers("/", "/index", "/search").permitAll()
                // 运动记录详情（公开浏览）
                .antMatchers("/moment/**").permitAll()
                // 运动项目库（公开浏览）
                .antMatchers("/projects").permitAll()
                .antMatchers("/api/projects").permitAll()
                // 运动计划浏览（公开）
                .antMatchers("/plans").permitAll()
                // 勋章馆（公开浏览）
                .antMatchers("/medals").permitAll()
                // 用户公开主页（仅数字 ID 路径）
                .regexMatchers("/user/\\d+").permitAll()
                .regexMatchers("/api/user/\\d+/.*").permitAll()
                // 管理后台仅 ADMIN
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 其他请求需要登录
                .anyRequest().authenticated()
            .and()
            // 表单登录
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/doLogin")
                .defaultSuccessUrl("/", true)
                .permitAll()
            .and()
            // 登出
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            .and()
            // 禁用 CSRF（开发环境，生产应按需开启）
            .csrf().disable();

        return http.build();
    }
}
