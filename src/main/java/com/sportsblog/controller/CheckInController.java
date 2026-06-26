package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CheckInService;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 独立打卡控制器（未来扩展用）
 */
@Controller
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /** 执行打卡（独立打卡，可附带运动记录ID） */
    @PostMapping("/api/checkin")
    @ResponseBody
    public Result<?> doCheckIn(@RequestParam Long childId,
                               @RequestParam(required = false) Long momentId) {
        User user = getCurrentUser();
        checkInService.checkIn(user.getId(), childId, momentId);
        return Result.success("打卡成功！");
    }

    /** 获取月度日历数据 */
    @GetMapping("/api/checkin/calendar")
    @ResponseBody
    public Result<?> getCalendar(@RequestParam Long childId,
                                 @RequestParam int year,
                                 @RequestParam int month) {
        User user = getCurrentUser();
        return Result.success(checkInService.getMonthlyCalendar(user.getId(), childId, year, month));
    }

    /** 获取当前连续天数 */
    @GetMapping("/api/checkin/streak")
    @ResponseBody
    public Result<?> getStreak(@RequestParam Long childId) {
        User user = getCurrentUser();
        return Result.success(checkInService.getCurrentStreak(user.getId(), childId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
