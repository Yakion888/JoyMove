package com.joymove.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.Result;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 通知 API 控制器
 */
@RestController
@RequestMapping("/user")
@Tag(name = "通知管理", description = "消息通知相关操作")
public class NotificationApiController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "标记全部已读")
    @PostMapping("/notifications/read-all")
    public Result<?> markAllRead() {
        notificationService.markAllRead(getCurrentUserId());
        return Result.success("已标记全部已读");
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, auth.getName());
        return userMapper.selectOne(wrapper).getId();
    }
}
