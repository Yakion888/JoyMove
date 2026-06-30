package com.joymove.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.common.Result;
import com.joymove.common.ResultCode;
import com.joymove.dto.MomentDetailVO;
import com.joymove.entity.FamilyMoment;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.FamilyMomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * 运动记录 API 控制器
 */
@RestController
@Validated
@Tag(name = "运动打卡", description = "创建和管理运动记录")
public class FamilyMomentApiController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "创建打卡记录")
    @PostMapping("/api/moment/create")
    public Result<?> create(@RequestParam @NotNull Long projectId,
                            @RequestParam @NotNull Long childId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
                            @RequestParam(required = false) @Min(1) @Max(480) Integer duration,
                            @RequestParam(required = false) @Size(max = 200) String location,
                            @RequestParam(required = false) @Size(max = 2000) String content,
                            @RequestParam(required = false) @Min(1) @Max(4) Integer emotion,
                            @RequestParam(required = false) @Min(1) @Max(5) Integer stars,
                            @RequestParam(defaultValue = "1") Integer isPublic,
                            @RequestParam(required = false) @Size(max = 200) String tags,
                            @RequestParam(required = false) MultipartFile image) {
        User user = getCurrentUser();
        if (user == null) return Result.error(ResultCode.UNAUTHORIZED);
        FamilyMoment moment = new FamilyMoment();
        moment.setUserId(user.getId());
        moment.setProjectId(projectId);
        moment.setChildId(childId);
        moment.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        moment.setDuration(duration);
        moment.setLocation(location);
        moment.setContent(content);
        moment.setEmotion(emotion);
        moment.setStars(stars);
        moment.setIsPublic(isPublic);
        moment.setTags(tags);
        momentService.save(moment, image);
        return Result.success("打卡成功！🎉");
    }

    @Operation(summary = "创建打卡记录（旧路径兼容）")
    @PostMapping("/api/moment/save")
    public Result<?> save(@RequestParam @NotNull Long projectId,
                          @RequestParam @NotNull Long childId,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
                          @RequestParam(required = false) @Min(1) @Max(480) Integer duration,
                          @RequestParam(required = false) @Size(max = 200) String location,
                          @RequestParam(required = false) @Size(max = 2000) String content,
                          @RequestParam(required = false) @Min(1) @Max(4) Integer emotion,
                          @RequestParam(required = false) @Min(1) @Max(5) Integer stars,
                          @RequestParam(defaultValue = "1") Integer isPublic,
                          @RequestParam(required = false) @Size(max = 200) String tags,
                          @RequestParam(required = false) MultipartFile image) {
        return create(projectId, childId, recordDate, duration, location, content, emotion, stars, isPublic, tags, image);
    }

    @Operation(summary = "用户运动记录列表")
    @GetMapping("/api/moment/list")
    public Result<?> list(@RequestParam(defaultValue = "1") @Min(1) int page,
                          @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        User user = getCurrentUser();
        return Result.success(momentService.getUserMoments(user.getId(), page, size));
    }

    @Operation(summary = "月度运动日历")
    @GetMapping("/api/moment/calendar")
    public Result<?> calendar(@RequestParam @Min(2020) int year,
                              @RequestParam @Min(1) @Max(12) int month) {
        User user = getCurrentUser();
        return Result.success(momentService.getUserCalendar(user.getId(), year, month));
    }

    @Operation(summary = "运动记录详情 JSON")
    @GetMapping("/api/moment/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        MomentDetailVO vo = momentService.getDetailById(id);
        return vo != null ? Result.success(vo) : Result.error("记录不存在");
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/api/moment/like")
    public Result<?> like(@RequestParam @NotNull Long momentId) {
        User user = getCurrentUser();
        boolean liked = momentService.toggleLike(momentId, user.getId());
        return Result.success(liked ? "已点赞" : "已取消");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
