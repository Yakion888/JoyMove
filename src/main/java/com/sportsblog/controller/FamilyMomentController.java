package com.sportsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.common.Result;
import com.sportsblog.dto.MomentDetailVO;
import com.sportsblog.entity.*;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.mapper.UserMapper;
import com.sportsblog.service.CommunityInteractionService;
import com.sportsblog.service.FamilyMomentService;
import com.sportsblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 运动记录控制器 — 打卡 = 发布运动记录
 */
@Controller
public class FamilyMomentController {

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private CommunityInteractionService interactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    // ==================== 页面 ====================

    /** 打卡页面（运动记录表单 + 日历） */
    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("children", userService.getChildren(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "checkin";
    }

    /** 发布页面 */
    @GetMapping("/moment/publish")
    public String publishPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("children", userService.getChildren(user.getId()));
        model.addAttribute("projects", projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1)));
        return "checkin";
    }

    /** 运动记录详情页 */
    @GetMapping("/moment/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("moment", momentService.getDetailById(id));
        model.addAttribute("interactions", interactionService.getTreeFlat(id));
        User currentUser = getCurrentUser();
        if (currentUser != null) model.addAttribute("currentUser", currentUser);
        return "moment-detail";
    }

    // ==================== API ====================

    /** 创建运动记录（打卡） */
    @PostMapping("/api/moment/create")
    @ResponseBody
    public Result<?> create(@RequestParam Long projectId,
                            @RequestParam Long childId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
                            @RequestParam(required = false) Integer duration,
                            @RequestParam(required = false) String location,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) Integer emotion,
                            @RequestParam(required = false) Integer stars,
                            @RequestParam(defaultValue = "1") Integer isPublic,
                            @RequestParam(required = false) String tags,
                            @RequestParam(required = false) MultipartFile image) {
        User user = getCurrentUser();
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

    /** 兼容旧路径 */
    @PostMapping("/api/moment/save")
    @ResponseBody
    public Result<?> save(@RequestParam Long projectId,
                          @RequestParam Long childId,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
                          @RequestParam(required = false) Integer duration,
                          @RequestParam(required = false) String location,
                          @RequestParam(required = false) String content,
                          @RequestParam(required = false) Integer emotion,
                          @RequestParam(required = false) Integer stars,
                          @RequestParam(defaultValue = "1") Integer isPublic,
                          @RequestParam(required = false) String tags,
                          @RequestParam(required = false) MultipartFile image) {
        return create(projectId, childId, recordDate, duration, location, content, emotion, stars, isPublic, tags, image);
    }

    /** 获取用户运动记录列表 */
    @GetMapping("/api/moment/list")
    @ResponseBody
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        User user = getCurrentUser();
        return Result.success(momentService.getUserMoments(user.getId(), page, size));
    }

    /** 获取用户月度运动日历 */
    @GetMapping("/api/moment/calendar")
    @ResponseBody
    public Result<?> calendar(@RequestParam int year,
                              @RequestParam int month) {
        User user = getCurrentUser();
        return Result.success(momentService.getUserCalendar(user.getId(), year, month));
    }

    /** 获取单条记录详情（JSON） */
    @GetMapping("/api/moment/{id}")
    @ResponseBody
    public Result<?> getDetail(@PathVariable Long id) {
        MomentDetailVO vo = momentService.getDetailById(id);
        return vo != null ? Result.success(vo) : Result.error("记录不存在");
    }

    /** 点赞/取消点赞 */
    @PostMapping("/api/moment/like")
    @ResponseBody
    public Result<?> like(@RequestParam Long momentId) {
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
