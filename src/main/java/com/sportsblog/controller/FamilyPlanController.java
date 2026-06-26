package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.FamilyPlan;
import com.sportsblog.entity.SportProject;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.FamilyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 运动计划控制器
 */
@Controller
public class FamilyPlanController {

    @Autowired
    private FamilyPlanService planService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    /** 计划列表页 */
    @GetMapping("/plans")
    public String plansPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("plans", planService.getByUserId(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "plans";
    }

    /** 创建计划 */
    @PostMapping("/api/plan/create")
    @ResponseBody
    public Result<?> create(@RequestParam Long projectId,
                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate plannedDate) {
        User user = getCurrentUser();
        FamilyPlan plan = new FamilyPlan();
        plan.setUserId(user.getId());
        plan.setProjectId(projectId);
        plan.setPlannedDate(plannedDate);
        planService.create(plan);
        return Result.success("计划创建成功");
    }

    /** 完成计划 */
    @PostMapping("/api/plan/complete")
    @ResponseBody
    public Result<?> complete(@RequestParam Long planId,
                              @RequestParam(required = false) Integer actualDuration,
                              @RequestParam(required = false) String childFeedback) {
        planService.complete(planId, actualDuration, childFeedback);
        return Result.success("计划已完成！");
    }

    /** 跳过计划 */
    @PostMapping("/api/plan/skip")
    @ResponseBody
    public Result<?> skip(@RequestParam Long planId) {
        planService.skip(planId);
        return Result.success("计划已跳过");
    }

    /** 按日期范围查询（日历视图） */
    @GetMapping("/api/plan/calendar")
    @ResponseBody
    public Result<?> getCalendar(@RequestParam String startDate,
                                 @RequestParam String endDate) {
        User user = getCurrentUser();
        return Result.success(planService.getByDateRange(user.getId(), startDate, endDate));
    }

    /** 运动指导页 */
    @GetMapping("/guide")
    public String guidePage(@RequestParam Long projectId, Model model) {
        model.addAttribute("project", projectMapper.selectById(projectId));
        return "guide";
    }

    /** 浏览运动项目库（页面） */
    @GetMapping("/projects")
    public String projectsPage(Model model) {
        return "projects";
    }

    /** 运动项目库 JSON */
    @GetMapping("/api/projects")
    @ResponseBody
    public Result<?> projectsJson() {
        return Result.success(projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
