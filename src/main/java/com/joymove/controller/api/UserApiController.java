package com.joymove.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.BusinessException;
import com.joymove.common.Result;
import com.joymove.entity.ChildProfile;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 用户 API 控制器
 */
@RestController
@Validated
@Tag(name = "用户管理", description = "注册、登录、个人资料与孩子管理")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "用户注册")
    @PostMapping("/api/auth/register")
    public Result<?> register(@RequestParam @NotBlank @Size(min = 3, max = 50) String username,
                              @RequestParam @NotBlank @Size(min = 6) String password,
                              @RequestParam @NotBlank @Size(max = 50) String nickname,
                              @RequestParam(required = false) String childName,
                              @RequestParam(required = false) Integer childGender,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate childBirth,
                              @RequestParam(required = false) Integer familyRole) {
        try {
            User user = userService.register(username.trim(), password, nickname.trim());
            if (familyRole != null) {
                user.setFamilyRole(familyRole);
                userService.updateProfile(user);
            }
            if (childName != null && !childName.trim().isEmpty()) {
                ChildProfile child = new ChildProfile();
                child.setUserId(user.getId());
                child.setName(childName.trim());
                if (childGender != null) child.setGender(childGender);
                if (childBirth != null) child.setBirthDate(childBirth);
                userService.addChild(child);
            }
            return Result.success("注册成功，请登录");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新个人资料")
    @PostMapping("/user/profile/update")
    public Result<?> updateProfile(@RequestParam(required = false) String nickname,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String city,
                                   @RequestParam(required = false) String bio,
                                   @RequestParam(required = false) Integer familyRole) {
        User user = getCurrentUser();
        if (nickname != null) user.setNickname(nickname);
        if (phone != null) user.setPhone(phone);
        if (city != null) user.setCity(city);
        if (bio != null) user.setBio(bio);
        if (familyRole != null) user.setFamilyRole(familyRole);
        userService.updateProfile(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取孩子列表 JSON")
    @GetMapping("/api/user/children")
    public Result<?> childrenJson() {
        User user = getCurrentUser();
        return Result.success(userService.getChildren(user.getId()));
    }

    @Operation(summary = "添加孩子")
    @PostMapping("/user/child/add")
    public Result<?> addChild(@RequestParam @NotBlank String name,
                              @RequestParam @Min(0) @Max(1) Integer gender,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate) {
        User user = getCurrentUser();
        ChildProfile child = new ChildProfile();
        child.setUserId(user.getId());
        child.setName(name);
        child.setGender(gender);
        child.setBirthDate(birthDate);
        userService.addChild(child);
        return Result.success("孩子信息添加成功");
    }

    @Operation(summary = "更新孩子信息")
    @PostMapping("/user/child/update")
    public Result<?> updateChild(@RequestParam @NotNull Long childId,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) Integer gender,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate) {
        ChildProfile child = userService.getChildById(childId);
        if (child == null) return Result.error("孩子信息不存在");
        if (name != null) child.setName(name);
        if (gender != null) child.setGender(gender);
        if (birthDate != null) child.setBirthDate(birthDate);
        userService.updateChild(child);
        return Result.success("孩子信息更新成功");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, auth.getName());
        return userMapper.selectOne(wrapper);
    }
}
