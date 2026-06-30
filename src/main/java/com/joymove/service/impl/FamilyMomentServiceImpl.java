package com.joymove.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joymove.dto.FamilyStatsDTO;
import com.joymove.dto.MomentDetailVO;
import com.joymove.dto.MonthlyCalendarDTO;
import com.joymove.entity.*;
import com.joymove.mapper.*;
import com.joymove.service.FamilyMomentService;
import com.joymove.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FamilyMomentServiceImpl implements FamilyMomentService {

    @Autowired
    private FamilyMomentMapper momentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChildProfileMapper childProfileMapper;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private MedalService medalService;

    @Autowired
    private MomentLikeMapper likeMapper;

    @Value("${app.upload.path:/uploads/}")
    private String uploadPath;

    @Override
    @Transactional
    public FamilyMoment save(FamilyMoment moment, MultipartFile file) {
        moment.setStatus(1); // demo直接发布
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        if (moment.getIsPublic() == null) moment.setIsPublic(1);
        if (moment.getRecordDate() == null) moment.setRecordDate(LocalDate.now());

        // 快照孩子年龄
        if (moment.getChildId() != null) {
            ChildProfile child = childProfileMapper.selectById(moment.getChildId());
            if (child != null && child.getBirthDate() != null) {
                moment.setChildAgeAtMoment(
                    LocalDate.now().getYear() - child.getBirthDate().getYear());
            }
        }

        if (file != null && !file.isEmpty()) {
            moment.setImageUrl(uploadImage(file));
        }

        momentMapper.insert(moment);

        // 更新用户统计数据
        updateUserStats(moment.getUserId());

        // 触发勋章检查
        medalService.checkAndAward(moment.getUserId());
        log.info("[ASYNC] Saved & dispatched: id={}, userId={}", moment.getId(), moment.getUserId());

        return moment;
    }

    /** 更新用户累计运动天数和最长连续天数 */
    private void updateUserStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        int totalDays = momentMapper.countDistinctRecordDates(userId);
        int streak = calcCurrentStreak(userId);

        user.setTotalDays(totalDays);
        if (streak > (user.getLongestStreak() != null ? user.getLongestStreak() : 0)) {
            user.setLongestStreak(streak);
        }
        userMapper.updateById(user);
    }

    /** 计算截至今天（或最后记录日期）的连续天数 */
    private int calcCurrentStreak(Long userId) {
        List<String> dates = momentMapper.selectDistinctRecordDates(userId);
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        // 如果最早记录不是今天或昨天，从最近日期开始检查
        LocalDate cursor = LocalDate.parse(dates.get(0)); // 最近的日期
        if (!cursor.equals(today) && !cursor.equals(today.minusDays(1))) {
            // 最近记录不在今天/昨天，streak = 1
            return 1;
        }

        int streak = 1;
        LocalDate expected = cursor.minusDays(1);
        for (int i = 1; i < dates.size(); i++) {
            LocalDate d = LocalDate.parse(dates.get(i));
            if (d.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    @Override
    @Transactional
    public void update(FamilyMoment moment, MultipartFile file) {
        moment.setStatus(1); // demo直接发布
        if (file != null && !file.isEmpty()) {
            moment.setImageUrl(uploadImage(file));
        }
        momentMapper.updateById(moment);
    }

    @Override
    public IPage<MomentDetailVO> getPublishedPage(int pageNum, int pageSize, Long projectId) {
        Page<FamilyMoment> page = new Page<>(pageNum, pageSize);
        IPage<FamilyMoment> result = momentMapper.selectPublishedPage(page, projectId);
        return result.convert(this::toDetailVO);
    }

    @Override
    public MomentDetailVO getDetailById(Long id) {
        FamilyMoment moment = momentMapper.selectDetailById(id);
        return moment != null ? toDetailVO(moment) : null;
    }

    @Override
    public void like(Long id) {
        momentMapper.incrementLikeCount(id);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long momentId, Long userId) {
        if (likeMapper.existsByUserAndMoment(userId, momentId) > 0) {
            // 取消点赞
            likeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MomentLike>()
                    .eq(MomentLike::getMomentId, momentId).eq(MomentLike::getUserId, userId));
            FamilyMoment m = momentMapper.selectById(momentId);
            if (m != null && m.getLikeCount() > 0) {
                m.setLikeCount(m.getLikeCount() - 1);
                momentMapper.updateById(m);
            }
            return false;
        } else {
            MomentLike like = new MomentLike();
            like.setMomentId(momentId);
            like.setUserId(userId);
            likeMapper.insert(like);
            momentMapper.incrementLikeCount(momentId);
            return true;
        }
    }

    @Override
    public IPage<MomentDetailVO> getMomentsByTag(String tag, int page, int size) {
        Page<FamilyMoment> p = new Page<>(page, size);
        Page<FamilyMoment> result = (Page<FamilyMoment>) momentMapper.selectPage(p,
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMoment>()
                .eq(FamilyMoment::getStatus, 1)
                .eq(FamilyMoment::getIsPublic, 1)
                .like(FamilyMoment::getTags, tag)
                .orderByDesc(FamilyMoment::getCreateTime));
        return result.convert(this::toDetailVO);
    }

    @Override
    public List<String> getHotTags(int limit) {
        Map<String, Integer> freq = new java.util.LinkedHashMap<>();
        try {
            List<FamilyMoment> all = momentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMoment>()
                    .eq(FamilyMoment::getStatus, 1).eq(FamilyMoment::getIsPublic, 1));
            for (FamilyMoment m : all) {
                if (m.getTags() != null && !m.getTags().isEmpty()) {
                    for (String t : m.getTags().split(",")) {
                        t = t.trim();
                        if (!t.isEmpty()) freq.merge(t, 1, Integer::sum);
                    }
                }
            }
        } catch (Exception ignored) {}
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit).map(Map.Entry::getKey).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public IPage<MomentDetailVO> getUserMoments(Long userId, int pageNum, int pageSize) {
        return getByUserIdPage(pageNum, pageSize, userId);
    }

    @Override
    public MonthlyCalendarDTO getUserCalendar(Long userId, int year, int month) {
        List<FamilyMoment> records = momentMapper.selectByUserIdAndMonth(userId, year, month);

        // 按日期分组（取每天第一条记录的信息）
        Map<LocalDate, FamilyMoment> dateMap = new LinkedHashMap<>();
        for (FamilyMoment m : records) {
            if (m.getRecordDate() != null) {
                dateMap.putIfAbsent(m.getRecordDate(), m);
            }
        }

        // 生成当月所有日期
        YearMonth ym = YearMonth.of(year, month);
        List<MonthlyCalendarDTO.CalendarDay> days = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            MonthlyCalendarDTO.CalendarDay cd = new MonthlyCalendarDTO.CalendarDay();
            cd.setDate(date.toString());
            FamilyMoment m = dateMap.get(date);
            if (m != null) {
                cd.setHasRecord(true);
                if (m.getProjectId() != null) {
                    SportProject proj = projectMapper.selectById(m.getProjectId());
                    cd.setProjectName(proj != null ? proj.getName() : null);
                }
                cd.setEmotionEmoji(emotionToEmoji(m.getEmotion()));
                cd.setStars(m.getStars());
            } else {
                cd.setHasRecord(false);
            }
            days.add(cd);
        }

        // 计算 streak（截至本月最后一天或今天）
        LocalDate endDate = ym.atEndOfMonth();
        if (endDate.isAfter(LocalDate.now())) endDate = LocalDate.now();
        int streak = calcCurrentStreak(userId);

        MonthlyCalendarDTO dto = new MonthlyCalendarDTO();
        dto.setYear(year);
        dto.setMonth(month);
        dto.setDays(days);
        dto.setTotalDays(dateMap.size());
        dto.setStreakDays(streak);
        dto.setTotalActivities(momentMapper.countByUserId(userId));
        return dto;
    }

    private String emotionToEmoji(Integer emotion) {
        if (emotion == null) return null;
        switch (emotion) {
            case 1: return "😄";
            case 2: return "😊";
            case 3: return "😐";
            case 4: return "😓";
            default: return null;
        }
    }

    @Override
    public IPage<MomentDetailVO> getByUserIdPage(int pageNum, int pageSize, Long userId) {
        Page<FamilyMoment> page = new Page<>(pageNum, pageSize);
        IPage<FamilyMoment> result = momentMapper.selectByUserIdPage(page, userId);
        return result.convert(this::toDetailVO);
    }

    @Override
    public IPage<MomentDetailVO> search(int pageNum, int pageSize, String keyword) {
        Page<FamilyMoment> page = new Page<>(pageNum, pageSize);
        IPage<FamilyMoment> result = momentMapper.searchPublished(page, keyword);
        return result.convert(this::toDetailVO);
    }

    @Override
    public void delete(Long id) {
        momentMapper.deleteById(id);
        log.info("FamilyMoment deleted: id={}", id);
    }

    @Override
    public void approve(Long id) {
        FamilyMoment m = momentMapper.selectById(id);
        if (m != null) { m.setStatus(1); momentMapper.updateById(m); }
    }

    @Override
    public void reject(Long id, String reason) {
        FamilyMoment m = momentMapper.selectById(id);
        if (m != null) { m.setStatus(2); momentMapper.updateById(m); }
    }

    @Override
    public FamilyStatsDTO getFamilyStats(Long userId) {
        FamilyStatsDTO stats = new FamilyStatsDTO();
        stats.setTotalActivities(countByUserId(userId));
        stats.setProjectVariety(countDistinctProjects(userId));
        return stats;
    }

    @Override
    public int countByUserId(Long userId) { return momentMapper.countByUserId(userId); }

    @Override
    public int countDistinctProjects(Long userId) { return momentMapper.countDistinctProjects(userId); }

    @Override
    public int countByProjectId(Long userId, Long projectId) { return momentMapper.countByProjectId(userId, projectId); }

    @Override
    public int countByProjectIds(Long userId, List<Long> projectIds) { return momentMapper.countByProjectIds(userId, projectIds); }

    @Override
    public int countByEmotion(Long userId, Integer emotion) { return momentMapper.countByEmotion(userId, emotion); }

    private MomentDetailVO toDetailVO(FamilyMoment m) {
        MomentDetailVO vo = new MomentDetailVO();
        copyFields(m, vo);
        if (m.getUserId() != null) {
            User user = userMapper.selectById(m.getUserId());
            if (user != null) { vo.setAuthorNickname(user.getNickname()); vo.setAuthorAvatar(user.getAvatar()); }
        }
        if (m.getChildId() != null) {
            ChildProfile child = childProfileMapper.selectById(m.getChildId());
            if (child != null) vo.setChildName(child.getName());
        }
        if (m.getProjectId() != null) {
            SportProject proj = projectMapper.selectById(m.getProjectId());
            if (proj != null) vo.setProjectName(proj.getName());
        }
        return vo;
    }

    private void copyFields(FamilyMoment src, MomentDetailVO dst) {
        dst.setId(src.getId()); dst.setUserId(src.getUserId()); dst.setProjectId(src.getProjectId());
        dst.setChildId(src.getChildId()); dst.setChildAgeAtMoment(src.getChildAgeAtMoment());
        dst.setDuration(src.getDuration()); dst.setLocation(src.getLocation());
        dst.setContent(src.getContent()); dst.setImageUrl(src.getImageUrl());
        dst.setEmotion(src.getEmotion()); dst.setStars(src.getStars());
        dst.setStatus(src.getStatus()); dst.setIsPublic(src.getIsPublic());
        dst.setLikeCount(src.getLikeCount()); dst.setCommentCount(src.getCommentCount());
        dst.setRecordDate(src.getRecordDate());
        dst.setCreateTime(src.getCreateTime()); dst.setUpdateTime(src.getUpdateTime());
    }

    private String uploadImage(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().substring(0, 8) + extension;
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(fileName).toFile());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
}
