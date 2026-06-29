package com.sportsblog.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.SportProject;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.FamilyMomentMapper;
import com.sportsblog.mapper.NotificationMapper;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.FamilyMomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 首页控制器 — 悦动宝
 */
@Controller
public class IndexController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @GetMapping({"/", "/index"})
    public String index(@RequestParam(required = false) Long projectId,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        // 公开活动流
        model.addAttribute("moments", momentService.getPublishedPage(page, 10, projectId).getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("projectId", projectId);

        // 运动项目列表
        List<SportProject> projects = projectMapper.selectList(null);
        model.addAttribute("projects", projects);

        // 当前用户信息
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("unreadCount", notificationMapper.countUnread(currentUser.getId()));
        }
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "1") int page, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) return "redirect:/";
        model.addAttribute("moments", momentService.search(page, 10, keyword.trim()).getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword.trim());
        return "search";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
