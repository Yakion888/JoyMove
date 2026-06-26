package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.AIMonthlyReportService;
import com.sportsblog.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * AI 功能控制器
 */
@Controller
public class AIController {

    @Autowired
    private AIRecommendationService recommendationService;

    @Autowired
    private AIMonthlyReportService reportService;

    @Autowired
    private UserMapper userMapper;

    /** AI 运动推荐 */
    @GetMapping("/api/ai/recommend")
    @ResponseBody
    public Result<?> recommend(@RequestParam Long childId,
                               @RequestParam(defaultValue = "晴天") String weather,
                               @RequestParam(defaultValue = "3") int count) {
        return Result.success(recommendationService.recommend(childId, weather, count));
    }

    /** AI 月度成长报告 */
    @GetMapping("/api/ai/report")
    @ResponseBody
    public Result<?> report(@RequestParam Long childId,
                            @RequestParam int year,
                            @RequestParam int month) {
        User user = getCurrentUser();
        return Result.success(reportService.generateReport(user.getId(), childId, year, month));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
