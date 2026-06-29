package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.AIMonthlyReportService;
import com.sportsblog.service.AIRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * AI 功能控制器
 */
@RestController
@Validated
@Tag(name = "AI 功能", description = "AI 运动推荐与成长报告")
public class AIController {

    @Autowired
    private AIRecommendationService recommendationService;

    @Autowired
    private AIMonthlyReportService reportService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "AI 运动推荐", description = "根据孩子、天气条件推荐运动项目")
    @GetMapping("/api/ai/recommend")
    public Result<?> recommend(@RequestParam @NotNull Long childId,
                               @RequestParam(defaultValue = "晴天") String weather,
                               @RequestParam(defaultValue = "3") @Min(1) @Max(10) int count) {
        return Result.success(recommendationService.recommend(childId, weather, count));
    }

    @Operation(summary = "AI 月度成长报告", description = "生成指定孩子指定月份的成长报告")
    @GetMapping("/api/ai/report")
    public Result<?> report(@RequestParam @NotNull Long childId,
                            @RequestParam @Min(2020) int year,
                            @RequestParam @Min(1) @Max(12) int month) {
        User user = getCurrentUser();
        return Result.success(reportService.generateReport(user.getId(), childId, year, month));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
