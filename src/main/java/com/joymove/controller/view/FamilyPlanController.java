package com.joymove.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.entity.SportProject;
import com.joymove.entity.User;
import com.joymove.mapper.SportProjectMapper;
import com.joymove.mapper.UserMapper;
import com.joymove.service.FamilyPlanService;
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
        if (user == null) return "redirect:/login";
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
