package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.FamilyMomentService;
import com.sportsblog.service.CommunityInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 社区控制器
 */
@Controller
public class CommunityController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserMapper userMapper;

    /** 社区信息流页面 */
    @GetMapping("/community")
    public String feedPage(Model model) {
        User user = getCurrentUser();
        if (user != null) model.addAttribute("currentUser", user);
        return "community";
    }

    /** 社区信息流 API */
    @GetMapping("/api/community/feed")
    @ResponseBody
    public Result<?> feed(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String tag) {
        if (tag != null && !tag.isEmpty()) {
            return Result.success(momentService.getMomentsByTag(tag, page, size));
        }
        return Result.success(momentService.getPublishedPage(page, size, null));
    }

    /** 热门标签 */
    @GetMapping("/api/community/hot-tags")
    @ResponseBody
    public Result<?> hotTags() {
        return Result.success(momentService.getHotTags(10));
    }

    /** 点赞/取消点赞 */
    @PostMapping("/api/community/like")
    @ResponseBody
    public Result<?> like(@RequestParam Long momentId) {
        User user = getCurrentUser();
        boolean liked = momentService.toggleLike(momentId, user.getId());
        return Result.success(liked ? "已点赞" : "已取消");
    }

    /** 评论 */
    @PostMapping("/api/community/comment")
    @ResponseBody
    public Result<?> comment(@RequestParam Long momentId,
                             @RequestParam String content,
                             @RequestParam(required = false) Long parentId) {
        User user = getCurrentUser();
        interactionService.save(momentId, content, parentId, user.getId());
        return Result.success("评论成功");
    }

    /** 获取评论列表 */
    @GetMapping("/api/community/comments")
    @ResponseBody
    public Result<?> comments(@RequestParam Long momentId) {
        return Result.success(interactionService.getTreeFlat(momentId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
