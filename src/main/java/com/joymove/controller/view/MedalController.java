package com.joymove.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 勋章页面控制器
 */
@Controller
public class MedalController {

    @Autowired
    private MedalService medalService;

    @Autowired
    private UserMapper userMapper;

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

    @GetMapping("/my/medals")
    public String myMedalsPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("records", medalService.getUserRecords(user.getId()));
        model.addAttribute("progress", medalService.getProgress(user.getId()));
        model.addAttribute("allMedals", medalService.getAll());
        return "medals";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
