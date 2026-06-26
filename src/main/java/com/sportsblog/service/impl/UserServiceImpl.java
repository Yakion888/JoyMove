package com.sportsblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.BusinessException;
import com.sportsblog.entity.ChildProfile;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.ChildProfileMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChildProfileMapper childProfileMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String password, String nickname) {
        if (isUsernameExists(username)) {
            throw new BusinessException("用户名已被占用");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(0);
        user.setJoinDate(LocalDate.now());
        user.setTotalDays(0);
        user.setLongestStreak(0);
        userMapper.insert(user);
        return user;
    }

    @Override
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public User getProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateProfile(User user) {
        userMapper.updateById(user);
    }

    @Override
    public ChildProfile addChild(ChildProfile child) {
        childProfileMapper.insert(child);
        return child;
    }

    @Override
    public void updateChild(ChildProfile child) {
        childProfileMapper.updateById(child);
    }

    @Override
    public List<ChildProfile> getChildren(Long userId) {
        LambdaQueryWrapper<ChildProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChildProfile::getUserId, userId);
        return childProfileMapper.selectList(wrapper);
    }

    @Override
    public ChildProfile getChildById(Long childId) {
        return childProfileMapper.selectById(childId);
    }
}
