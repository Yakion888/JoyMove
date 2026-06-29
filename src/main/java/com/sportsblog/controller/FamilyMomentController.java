package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.SportProject;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CommunityInteractionService;
import com.sportsblog.service.FamilyMomentService;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 运动记录页面控制器
 */
@Controller
public class FamilyMomentController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("children", userService.getChildren(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "checkin";
    }

    @GetMapping("/moment/publish")
    public String publishPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("children", userService.getChildren(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "checkin";
    }

    @GetMapping("/moment/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("moment", momentService.getDetailById(id));
        model.addAttribute("interactions", interactionService.getTreeFlat(id));
        User currentUser = getCurrentUser();
        if (currentUser != null) model.addAttribute("currentUser", currentUser);
        return "moment-detail";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
