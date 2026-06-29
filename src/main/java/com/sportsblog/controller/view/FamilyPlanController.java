package com.sportsblog.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.SportProject;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.FamilyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 运动计划页面控制器
 */
@Controller
public class FamilyPlanController {

    @Autowired
    private FamilyPlanService planService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/plans")
    public String plansPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("plans", planService.getByUserId(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "plans";
    }

    @GetMapping("/guide")
    public String guidePage(@RequestParam Long projectId, Model model) {
        model.addAttribute("project", projectMapper.selectById(projectId));
        return "guide";
    }

    @GetMapping("/projects")
    public String projectsPage(Model model) {
        return "projects";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
