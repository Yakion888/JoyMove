package com.sportsblog.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 社区页面控制器
 */
@Controller
public class CommunityController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/community")
    public String feedPage(Model model) {
        User user = getCurrentUser();
        if (user != null) model.addAttribute("currentUser", user);
        return "community";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
