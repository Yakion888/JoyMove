package com.joymove.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.Result;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.MedalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 勋章 API 控制器
 */
@RestController
@Tag(name = "勋章管理", description = "勋章进度查询")
public class MedalApiController {

    @Autowired
    private MedalService medalService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "获取用户勋章进度")
    @GetMapping("/api/medals/progress")
    public Result<?> getProgress() {
        User user = getCurrentUser();
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(medalService.getProgress(user.getId()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
