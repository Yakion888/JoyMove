package com.sportsblog.service;

import com.sportsblog.dto.*;

import java.util.List;

public interface DashboardService {

    /** 看板总览 */
    DashboardOverviewDTO getOverview(Long userId);

    /** 周统计 */
    WeeklyStatsDTO getWeeklyStats(Long userId);

    /** 运动项目分布 */
    List<ProjectDistributionDTO> getProjectDistribution(Long userId);

    /** 月度趋势 */
    MonthlyTrendDTO getMonthlyTrend(Long userId, int months);

    /** 连续打卡 */
    StreakInfoDTO getStreakInfo(Long userId);

    /** 勋章汇总 */
    MedalSummaryDTO getMedalSummary(Long userId);

    /** 成长时间线 */
    GrowthTimelineDTO getGrowthTimeline(Long userId);
}
