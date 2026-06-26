package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 数据看板控制器
 */
@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserMapper userMapper;

    /** 看板页面 */
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    /** 看板总览 */
    @GetMapping("/api/dashboard/overview")
    @ResponseBody
    public Result<?> overview() {
        User user = getCurrentUser();
        return Result.success(dashboardService.getOverview(user.getId()));
    }

    /** 月度趋势 */
    @GetMapping("/api/dashboard/trend")
    @ResponseBody
    public Result<?> trend(@RequestParam(defaultValue = "6") int months) {
        User user = getCurrentUser();
        return Result.success(dashboardService.getMonthlyTrend(user.getId(), months));
    }

    /** 成长时间线 */
    @GetMapping("/api/dashboard/timeline")
    @ResponseBody
    public Result<?> timeline() {
        User user = getCurrentUser();
        return Result.success(dashboardService.getGrowthTimeline(user.getId()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
