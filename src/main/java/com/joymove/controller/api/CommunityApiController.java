package com.joymove.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.Result;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.CommunityInteractionService;
import com.joymove.service.FamilyMomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 社区 API 控制器
 */
@RestController
@Validated
@Tag(name = "社区动态", description = "信息流、点赞、评论")
public class CommunityApiController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "社区信息流", description = "分页获取公开运动记录")
    @GetMapping("/api/community/feed")
    public Result<?> feed(@RequestParam(defaultValue = "1") @Min(1) int page,
                          @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
                          @RequestParam(required = false) String tag) {
        if (tag != null && !tag.isEmpty()) {
            return Result.success(momentService.getMomentsByTag(tag, page, size));
        }
        return Result.success(momentService.getPublishedPage(page, size, null));
    }

    @Operation(summary = "热门标签")
    @GetMapping("/api/community/hot-tags")
    public Result<?> hotTags() {
        return Result.success(momentService.getHotTags(10));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/api/community/like")
    public Result<?> like(@RequestParam @NotNull Long momentId) {
        User user = getCurrentUser();
        boolean liked = momentService.toggleLike(momentId, user.getId());
        return Result.success(liked ? "已点赞" : "已取消");
    }

    @Operation(summary = "发表评论")
    @PostMapping("/api/community/comment")
    public Result<?> comment(@RequestParam @NotNull Long momentId,
                             @RequestParam @NotBlank @Size(max = 500) String content,
                             @RequestParam(required = false) Long parentId) {
        User user = getCurrentUser();
        interactionService.save(momentId, content, parentId, user.getId());
        return Result.success("评论成功");
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/api/community/comments")
    public Result<?> comments(@RequestParam @NotNull Long momentId) {
        return Result.success(interactionService.getTreeFlat(momentId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
