package com.sportsblog.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户页面控制器
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/user/profile")
    public String profilePage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("children", userService.getChildren(user.getId()));
        return "profile";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, auth.getName());
        return userMapper.selectOne(wrapper);
    }
}
