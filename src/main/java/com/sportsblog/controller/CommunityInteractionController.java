package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CommunityInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 社区互动控制器
 */
@RestController
@Validated
@Tag(name = "社区互动", description = "评论与回复")
public class CommunityInteractionController {

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "发布互动", description = "发表评论或回复")
    @PostMapping("/api/interaction/save")
    public Result<?> save(@RequestParam @NotNull Long momentId,
                          @RequestParam @NotBlank @Size(max = 500) String content,
                          @RequestParam(required = false) Long parentId) {
        User user = getCurrentUser();
        interactionService.save(momentId, content, parentId, user.getId());
        return Result.success("发布成功");
    }

    @Operation(summary = "获取互动列表", description = "获取指定动态的评论树")
    @GetMapping("/api/interaction/list")
    public Result<?> list(@RequestParam @NotNull Long momentId) {
        return Result.success(interactionService.getTreeFlat(momentId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
