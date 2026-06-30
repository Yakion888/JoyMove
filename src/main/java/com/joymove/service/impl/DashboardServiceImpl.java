package com.joymove.service.impl;

import com.joymove.dto.*;
import com.joymove.entity.*;
import com.joymove.mapper.*;
import com.joymove.service.DashboardService;
import com.joymove.service.MedalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private FamilyMomentMapper momentMapper;
    @Autowired
    private SportProjectMapper projectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MedalService medalService;
    @Autowired
    private ChildProfileMapper childProfileMapper;

    @Override
    public DashboardOverviewDTO getOverview(Long userId) {
        log.info("Dashboard overview requested: userId={}", userId);
        long start = System.currentTimeMillis();

        // 一次查询，同时用于 weeklyStats 和 projectDistribution
        List<FamilyMoment> allMoments = momentMapper.selectAllByUserId(userId);

        // 并行的两个独立查询
        CompletableFuture<StreakInfoDTO> streakFuture =
                CompletableFuture.supplyAsync(() -> getStreakInfo(userId));
        CompletableFuture<MedalSummaryDTO> medalFuture =
                CompletableFuture.supplyAsync(() -> getMedalSummary(userId));

        // 主线程：算 stats + distribution（共享 allMoments）
        DashboardOverviewDTO dto = new DashboardOverviewDTO();
        dto.setWeeklyStats(buildWeeklyStats(allMoments));
        dto.setProjectDistribution(buildProjectDistribution(allMoments));
        // 等并行结果
        dto.setStreakInfo(streakFuture.join());
        dto.setMedalSummary(medalFuture.join());

        log.info("Dashboard overview completed in {}ms", System.currentTimeMillis() - start);
        return dto;
    }

    @Override
    public WeeklyStatsDTO getWeeklyStats(Long userId) {
        return buildWeeklyStats(momentMapper.selectAllByUserId(userId));
    }

    @Override
    public List<ProjectDistributionDTO> getProjectDistribution(Long userId) {
        return buildProjectDistribution(momentMapper.selectAllByUserId(userId));
    }

    // ---------- 内部：共享 allMoments 的版本 ----------

    private WeeklyStatsDTO buildWeeklyStats(List<FamilyMoment> all) {
        WeeklyStatsDTO dto = new WeeklyStatsDTO();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        int weekCount = 0, weekDur = 0, monthCount = 0, monthDur = 0, totalCount = 0, totalDur = 0;
        double emotionSum = 0, starsSum = 0;
        int emotionCount = 0;

        for (FamilyMoment m : all) {
            if (m.getRecordDate() == null) continue;
            totalCount++;
            if (m.getDuration() != null) totalDur += m.getDuration();
            if (m.getEmotion() != null) { emotionSum += m.getEmotion(); emotionCount++; }
            if (m.getStars() != null) starsSum += m.getStars();

            if (!m.getRecordDate().isBefore(weekStart)) {
                weekCount++;
                if (m.getDuration() != null) weekDur += m.getDuration();
            }
            if (!m.getRecordDate().isBefore(monthStart)) {
                monthCount++;
                if (m.getDuration() != null) monthDur += m.getDuration();
            }
        }

        dto.setWeeklyCount(weekCount);
        dto.setWeeklyDuration(weekDur);
        dto.setMonthlyCount(monthCount);
        dto.setMonthlyDuration(monthDur);
        dto.setTotalCount(totalCount);
        dto.setTotalDuration(totalDur);
        dto.setAvgEmotion(emotionCount > 0 ? Math.round(emotionSum / emotionCount * 10.0) / 10.0 : 0);
        dto.setAvgStars(totalCount > 0 ? Math.round(starsSum / totalCount * 10.0) / 10.0 : 0);
        return dto;
    }

    private List<ProjectDistributionDTO> buildProjectDistribution(List<FamilyMoment> all) {
        Map<Long, Integer> countMap = new LinkedHashMap<>();
        int total = 0;
        for (FamilyMoment m : all) {
            if (m.getProjectId() != null) {
                countMap.merge(m.getProjectId(), 1, Integer::sum);
                total++;
            }
        }

        List<ProjectDistributionDTO> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : countMap.entrySet()) {
            SportProject proj = projectMapper.selectById(e.getKey());
            ProjectDistributionDTO dto = new ProjectDistributionDTO();
            dto.setProjectName(proj != null ? proj.getName() : "未知");
            dto.setProjectIcon(null);
            dto.setCount(e.getValue());
            dto.setPercentage(Math.round(e.getValue() * 1000.0 / total) / 10.0);
            result.add(dto);
        }
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return result;
    }

    // ---------- 其余方法不变 ----------

    @Override
    public MonthlyTrendDTO getMonthlyTrend(Long userId, int months) {
        MonthlyTrendDTO dto = new MonthlyTrendDTO();
        List<String> monthLabels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();

        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            monthLabels.add(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")));

            List<FamilyMoment> records = momentMapper.selectByUserIdAndMonth(userId, ym.getYear(), ym.getMonthValue());
            counts.add(records.size());
            durations.add(records.stream().mapToInt(m -> m.getDuration() != null ? m.getDuration() : 0).sum());
        }

        dto.setMonths(monthLabels);
        dto.setCounts(counts);
        dto.setDurations(durations);
        return dto;
    }

    @Override
    public StreakInfoDTO getStreakInfo(Long userId) {
        User user = userMapper.selectById(userId);
        List<String> dates = momentMapper.selectDistinctRecordDates(userId);

        StreakInfoDTO dto = new StreakInfoDTO();
        dto.setTotalDays(dates.size());
        dto.setLongestStreak(user != null && user.getLongestStreak() != null ? user.getLongestStreak() : 0);

        int streak = 0;
        if (!dates.isEmpty()) {
            LocalDate cursor = LocalDate.parse(dates.get(0));
            LocalDate today = LocalDate.now();
            if (cursor.equals(today) || cursor.equals(today.minusDays(1))) {
                streak = 1;
                LocalDate expected = cursor.minusDays(1);
                for (int i = 1; i < dates.size(); i++) {
                    LocalDate d = LocalDate.parse(dates.get(i));
                    if (d.equals(expected)) { streak++; expected = expected.minusDays(1); }
                    else break;
                }
            }
        }
        dto.setCurrentStreak(streak);

        YearMonth ym = YearMonth.now();
        dto.setMonthlyCheckDays(momentMapper.selectByUserIdAndMonth(userId, ym.getYear(), ym.getMonthValue()).size());

        return dto;
    }

    @Override
    public MedalSummaryDTO getMedalSummary(Long userId) {
        List<MedalProgressDTO> progress = medalService.getProgress(userId);
        MedalSummaryDTO dto = new MedalSummaryDTO();
        dto.setTotalCount(progress.size());
        dto.setEarnedCount((int) progress.stream().filter(MedalProgressDTO::getEarned).count());
        dto.setMedals(progress);
        return dto;
    }

    @Override
    public GrowthTimelineDTO getGrowthTimeline(Long userId) {
        List<FamilyMoment> all = momentMapper.selectByUserIdPage(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 50), userId).getRecords();

        GrowthTimelineDTO dto = new GrowthTimelineDTO();
        List<GrowthTimelineDTO.TimelineNode> nodes = new ArrayList<>();

        for (FamilyMoment m : all) {
            GrowthTimelineDTO.TimelineNode node = new GrowthTimelineDTO.TimelineNode();
            node.setDate(m.getRecordDate() != null ? m.getRecordDate().toString() : "");
            node.setContent(m.getContent() != null ? m.getContent() : "");
            node.setStars(m.getStars());
            node.setLocation(m.getLocation());
            node.setEmotionEmoji(emotionToEmoji(m.getEmotion()));

            if (m.getProjectId() != null) {
                SportProject proj = projectMapper.selectById(m.getProjectId());
                if (proj != null) {
                    node.setProjectName(proj.getName());
                    node.setProjectIcon(proj.getCoverImage());
                }
            }
            if (m.getChildId() != null) {
                ChildProfile child = childProfileMapper.selectById(m.getChildId());
                if (child != null) node.setChildName(child.getName());
            }
            nodes.add(node);
        }
        dto.setNodes(nodes);
        return dto;
    }

    private String emotionToEmoji(Integer e) {
        if (e == null) return null;
        switch (e) { case 1: return "😄"; case 2: return "😊"; case 3: return "😐"; case 4: return "😓"; default: return null; }
    }
}
