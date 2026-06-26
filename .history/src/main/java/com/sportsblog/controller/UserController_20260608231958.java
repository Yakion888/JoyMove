package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.BusinessException;
import com.sportsblog.common.Result;
import com.sportsblog.entity.SportTeam;
import com.sportsblog.entity.SysUser;
import com.sportsblog.mapper.SportTeamMapper;
import com.sportsblog.mapper.SysUserMapper;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户认证控制器
 */
@Controller
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SportTeamMapper sportTeamMapper;

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 用户注册接口
     * POST /api/auth/register
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public Result<?> register(@RequestParam @NotBlank(message = "用户名不能为空")
                              @Size(min = 3, max = 50, message = "用户名需3-50位") String username,
                              @RequestParam @NotBlank(message = "密码不能为空")
                              @Size(min = 6, message = "密码至少6位") String password,
                              @RequestParam @NotBlank(message = "昵称不能为空")
                              @Size(max = 50, message = "昵称最长50位") String nickname) {
        try {
            SysUser user = userService.register(username.trim(), password, nickname.trim());
            return Result.success("注册成功，请登录");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置主队页面
     */
    @GetMapping("/user/favorite-team")
    public String favoriteTeamPage(Model model) {
        List<SportTeam> teams = sportTeamMapper.selectList(null);
        SysUser user = getCurrentUser();
        model.addAttribute("teams", teams);
        model.addAttribute("currentFavoriteId", user.getFavoriteTeamId());
        return "favorite-team";
    }

    /**
     * 保存主队设置
     */
    @PostMapping("/user/favorite-team")
    @ResponseBody
    public Result<?> setFavoriteTeam(@RequestParam Long teamId) {
        SysUser user = getCurrentUser();
        user.setFavoriteTeamId(teamId);
        sysUserMapper.updateById(user);
        return Result.success("主队设置成功");
    }

    private SysUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, auth.getName());
        return sysUserMapper.selectOne(wrapper);
    }
}
