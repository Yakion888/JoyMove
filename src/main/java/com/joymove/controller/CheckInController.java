package com.joymove.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.Result;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.CheckInService;
import com.joymove.service.UserService;
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
 * 独立打卡控制器（未来扩展用）
 */
@RestController
@Validated
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
    public Result<?> doCheckIn(@RequestParam @NotNull Long childId,
                               @RequestParam(required = false) Long momentId) {
        User user = getCurrentUser();
        checkInService.checkIn(user.getId(), childId, momentId);
        return Result.success("打卡成功！");
    }

    @Operation(summary = "获取月度日历", description = "查询指定月份的打卡日历")
    @GetMapping("/api/checkin/calendar")
    public Result<?> getCalendar(@RequestParam @NotNull Long childId,
                                 @RequestParam @Min(2020) int year,
                                 @RequestParam @Min(1) @Max(12) int month) {
        User user = getCurrentUser();
        return Result.success(checkInService.getMonthlyCalendar(user.getId(), childId, year, month));
    }

    @Operation(summary = "获取连续打卡天数", description = "查询当前连续打卡天数")
    @GetMapping("/api/checkin/streak")
    public Result<?> getStreak(@RequestParam @NotNull Long childId) {
        User user = getCurrentUser();
        return Result.success(checkInService.getCurrentStreak(user.getId(), childId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
