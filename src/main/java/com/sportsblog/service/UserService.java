package com.sportsblog.service;

import com.sportsblog.entity.ChildProfile;
import com.sportsblog.entity.User;

import java.util.List;

public interface UserService {

    User register(String username, String password, String nickname);

    boolean isUsernameExists(String username);

    User getProfile(Long userId);

    void updateProfile(User user);

    ChildProfile addChild(ChildProfile child);

    void updateChild(ChildProfile child);

    List<ChildProfile> getChildren(Long userId);

    ChildProfile getChildById(Long childId);
}
