package com.joymove.controller.view;

import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户公开主页 — 页面控制器
 */
@Controller
public class PublicProfileController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/user/{id}")
    public String profile(@PathVariable Long id, Model model) {
        User profileUser = userMapper.selectById(id);
        if (profileUser == null) return "redirect:/";
        model.addAttribute("profileUser", profileUser);
        return "public-profile";
    }
}
