package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CommunityInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 社区互动控制器
 */
@Controller
public class CommunityInteractionController {

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserMapper userMapper;

    /** 发布互动（评论/回复） */
    @PostMapping("/api/interaction/save")
    @ResponseBody
    public Result<?> save(@RequestParam Long momentId,
                          @RequestParam String content,
                          @RequestParam(required = false) Long parentId) {
        User user = getCurrentUser();
        interactionService.save(momentId, content, parentId, user.getId());
        return Result.success("发布成功");
    }

    /** 获取互动列表 */
    @GetMapping("/api/interaction/list")
    @ResponseBody
    public Result<?> list(@RequestParam Long momentId) {
        return Result.success(interactionService.getTreeFlat(momentId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
