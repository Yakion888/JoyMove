package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CheckInService;
import com.sportsblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 独立打卡控制器（未来扩展用）
 */
@RestController
@Tag(name = "独立打卡", description = "基于 check_in 表的独立打卡功能")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "执行打卡", description = "独立打卡，可附带运动记录 ID")
    @PostMapping("/api/checkin")
    public Result<?> doCheckIn(@RequestParam Long childId,
                               @RequestParam(required = false) Long momentId) {
        User user = getCurrentUser();
        checkInService.checkIn(user.getId(), childId, momentId);
        return Result.success("打卡成功！");
    }

    @Operation(summary = "获取月度日历", description = "查询指定月份的打卡日历")
    @GetMapping("/api/checkin/calendar")
    public Result<?> getCalendar(@RequestParam Long childId,
                                 @RequestParam int year,
                                 @RequestParam int month) {
        User user = getCurrentUser();
        return Result.success(checkInService.getMonthlyCalendar(user.getId(), childId, year, month));
    }

    @Operation(summary = "获取连续打卡天数", description = "查询当前连续打卡天数")
    @GetMapping("/api/checkin/streak")
    public Result<?> getStreak(@RequestParam Long childId) {
        User user = getCurrentUser();
        return Result.success(checkInService.getCurrentStreak(user.getId(), childId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
