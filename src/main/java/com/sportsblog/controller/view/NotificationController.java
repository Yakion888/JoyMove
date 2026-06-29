package com.sportsblog.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通知页面控制器
 */
@Controller
@RequestMapping("/user")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/notifications")
    public String notifications(@RequestParam(defaultValue = "1") int page, Model model) {
        Long userId = getCurrentUserId();
        IPage<?> result = notificationService.getByUserId(page, 15, userId);

        model.addAttribute("notifications", result.getRecords());
        model.addAttribute("currentPage", result.getCurrent());
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("unreadCount", notificationService.countUnread(userId));
        return "notifications";
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, auth.getName());
        return userMapper.selectOne(wrapper).getId();
    }
}
