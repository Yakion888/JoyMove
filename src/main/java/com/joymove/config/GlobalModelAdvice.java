package com.joymove.config;

import com.joymove.mapper.NotificationMapper;
import com.joymove.mapper.UserMapper;
import com.joymove.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局模型属性 — 让通知红点在所有页面生效
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @ModelAttribute("unreadCount")
    public Integer addUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return 0;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        if (user == null) return 0;
        return notificationMapper.countUnread(user.getId());
    }
}
