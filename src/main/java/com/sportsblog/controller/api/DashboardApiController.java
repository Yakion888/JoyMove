package com.sportsblog.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 数据看板 API 控制器
 */
@RestController
@Tag(name = "数据看板", description = "运动数据统计与趋势")
public class DashboardApiController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "看板总览", description = "本周统计、连续打卡、勋章汇总")
    @GetMapping("/api/dashboard/overview")
    public Result<?> overview() {
        User user = getCurrentUser();
        return Result.success(dashboardService.getOverview(user.getId()));
    }

    @Operation(summary = "月度趋势", description = "近 N 个月的运动次数和时长趋势")
    @GetMapping("/api/dashboard/trend")
    public Result<?> trend(@RequestParam(defaultValue = "6") int months) {
        User user = getCurrentUser();
        return Result.success(dashboardService.getMonthlyTrend(user.getId(), months));
    }

    @Operation(summary = "成长时间线", description = "最近的成长记录时间线")
    @GetMapping("/api/dashboard/timeline")
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
