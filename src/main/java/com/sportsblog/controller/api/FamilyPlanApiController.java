package com.sportsblog.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.entity.FamilyPlan;
import com.sportsblog.entity.SportProject;
import com.sportsblog.entity.User;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.FamilyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 运动计划 API 控制器
 */
@RestController
@Validated
@Tag(name = "运动计划", description = "创建和管理运动计划")
public class FamilyPlanApiController {

    @Autowired
    private FamilyPlanService planService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "创建运动计划")
    @PostMapping("/api/plan/create")
    public Result<?> create(@RequestParam @NotNull Long projectId,
                            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate plannedDate) {
        User user = getCurrentUser();
        FamilyPlan plan = new FamilyPlan();
        plan.setUserId(user.getId());
        plan.setProjectId(projectId);
        plan.setPlannedDate(plannedDate);
        planService.create(plan);
        return Result.success("计划创建成功");
    }

    @Operation(summary = "完成计划")
    @PostMapping("/api/plan/complete")
    public Result<?> complete(@RequestParam @NotNull Long planId,
                              @RequestParam(required = false) @Min(1) Integer actualDuration,
                              @RequestParam(required = false) @Size(max = 500) String childFeedback) {
        planService.complete(planId, actualDuration, childFeedback);
        return Result.success("计划已完成！");
    }

    @Operation(summary = "跳过计划")
    @PostMapping("/api/plan/skip")
    public Result<?> skip(@RequestParam @NotNull Long planId) {
        planService.skip(planId);
        return Result.success("计划已跳过");
    }

    @Operation(summary = "计划日历", description = "按日期范围查询计划")
    @GetMapping("/api/plan/calendar")
    public Result<?> getCalendar(@RequestParam @NotBlank String startDate,
                                 @RequestParam @NotBlank String endDate) {
        User user = getCurrentUser();
        return Result.success(planService.getByDateRange(user.getId(), startDate, endDate));
    }

    @Operation(summary = "运动项目列表 JSON")
    @GetMapping("/api/projects")
    public Result<?> projectsJson() {
        return Result.success(projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
