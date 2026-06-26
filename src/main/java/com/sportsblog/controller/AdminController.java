package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.*;
import com.sportsblog.mapper.*;
import com.sportsblog.service.SportProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员控制器 — 悦动宝
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

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

    /** 审核管理页面 */
    @GetMapping("/activities")
    public String auditPage() {
        return "admin-activities";
    }

    // ==================== 统计数据 ====================

    @GetMapping("/api/stats")
    @ResponseBody
    public Result<?> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userMapper.selectCount(null));
        map.put("momentCount", momentMapper.selectCount(null));
        map.put("pendingCount", momentMapper.selectCount(
            new LambdaQueryWrapper<FamilyMoment>().eq(FamilyMoment::getStatus, 0)));
        map.put("projectCount", projectMapper.selectCount(null));
        return Result.success(map);
    }

    // ==================== 审核 ====================

    /** 按状态查询记录 */
    @GetMapping("/api/moments")
    @ResponseBody
    public Result<?> listMoments(@RequestParam(defaultValue = "0") int status) {
        return Result.success(momentMapper.selectByStatus(status));
    }

    /** 审核 */
    @PostMapping("/activity/audit")
    @ResponseBody
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
            // 发驳回通知
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

    /** 彻底删除 */
    @DeleteMapping("/activities/{id}/permanent")
    @ResponseBody
    public Result<?> permanentDelete(@PathVariable Long id) {
        interactionMapper.delete(new LambdaQueryWrapper<CommunityInteraction>()
                .eq(CommunityInteraction::getMomentId, id));
        notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getRelatedId, id));
        momentMapper.deleteById(id);
        return Result.success("已彻底删除");
    }

    // ==================== 项目管理 ====================

    /** 保存/更新运动项目 */
    @PostMapping("/api/project/save")
    @ResponseBody
    public Result<?> saveProject(@RequestBody SportProject project) {
        projectService.saveOrUpdate(project);
        return Result.success("保存成功");
    }

    /** 删除运动项目 */
    @PostMapping("/api/project/delete")
    @ResponseBody
    public Result<?> deleteProject(@RequestParam Long id) {
        projectService.delete(id);
        return Result.success("已删除");
    }
}
