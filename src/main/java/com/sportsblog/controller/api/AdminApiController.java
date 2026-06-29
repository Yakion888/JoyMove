package com.sportsblog.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.*;
import com.sportsblog.mapper.*;
import com.sportsblog.service.SportProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理后台 API 控制器
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台", description = "审核、统计、项目管理")
public class AdminApiController {

    @Autowired
    private FamilyMomentMapper momentMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private CommunityInteractionMapper interactionMapper;
    @Autowired
    private SportProjectService projectService;
    @Autowired
    private SportProjectMapper projectMapper;
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "管理统计数据")
    @GetMapping("/api/stats")
    public Result<?> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userMapper.selectCount(null));
        map.put("momentCount", momentMapper.selectCount(null));
        map.put("pendingCount", momentMapper.selectCount(
            new LambdaQueryWrapper<FamilyMoment>().eq(FamilyMoment::getStatus, 0)));
        map.put("projectCount", projectMapper.selectCount(null));
        return Result.success(map);
    }

    @Operation(summary = "按状态查询运动记录")
    @GetMapping("/api/moments")
    public Result<?> listMoments(@RequestParam(defaultValue = "0") int status) {
        return Result.success(momentMapper.selectByStatus(status));
    }

    @Operation(summary = "审核运动记录", description = "通过或驳回")
    @PostMapping("/activity/audit")
    public Result<?> audit(@RequestParam Long momentId,
                           @RequestParam Integer status,
                           @RequestParam(required = false) String rejectReason) {
        FamilyMoment moment = momentMapper.selectById(momentId);
        if (moment == null) return Result.error("记录不存在");
        if (status == 1) {
            moment.setStatus(1);
            momentMapper.updateById(moment);
            return Result.success("已通过审核");
        } else {
            moment.setStatus(2);
            moment.setRejectReason(rejectReason);
            momentMapper.updateById(moment);
            Notification n = new Notification();
            n.setUserId(moment.getUserId());
            n.setType(1);
            n.setMessage("您的运动记录已被驳回" + (rejectReason != null ? "，理由：" + rejectReason : ""));
            n.setRelatedId(momentId);
            n.setIsRead(0);
            notificationMapper.insert(n);
            return Result.success("已驳回");
        }
    }

    @Operation(summary = "彻底删除运动记录")
    @DeleteMapping("/activities/{id}/permanent")
    public Result<?> permanentDelete(@PathVariable Long id) {
        interactionMapper.delete(new LambdaQueryWrapper<CommunityInteraction>()
                .eq(CommunityInteraction::getMomentId, id));
        notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getRelatedId, id));
        momentMapper.deleteById(id);
        return Result.success("已彻底删除");
    }

    @Operation(summary = "保存/更新运动项目")
    @PostMapping("/api/project/save")
    public Result<?> saveProject(@RequestBody SportProject project) {
        projectService.saveOrUpdate(project);
        return Result.success("保存成功");
    }

    @Operation(summary = "删除运动项目")
    @PostMapping("/api/project/delete")
    public Result<?> deleteProject(@RequestParam Long id) {
        projectService.delete(id);
        return Result.success("已删除");
    }
}
