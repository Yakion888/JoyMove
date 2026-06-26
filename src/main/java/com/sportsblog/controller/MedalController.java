package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 勋章控制器
 */
@Controller
public class MedalController {

    @Autowired
    private MedalService medalService;

    @Autowired
    private UserMapper userMapper;

    /** 勋章馆页面 */
    @GetMapping("/medals")
    public String medalsPage(Model model) {
        model.addAttribute("allMedals", medalService.getAll());
        User user = getCurrentUser();
        if (user != null) {
            model.addAttribute("progress", medalService.getProgress(user.getId()));
            model.addAttribute("currentUser", user);
        }
        return "medals";
    }

    /** 我的勋章（需登录） */
    @GetMapping("/my/medals")
    public String myMedalsPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("records", medalService.getUserRecords(user.getId()));
        model.addAttribute("progress", medalService.getProgress(user.getId()));
        model.addAttribute("allMedals", medalService.getAll());
        return "medals";
    }

    /** 获取用户勋章进度 API */
    @GetMapping("/api/medals/progress")
    @ResponseBody
    public Result<?> getProgress() {
        User user = getCurrentUser();
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(medalService.getProgress(user.getId()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
