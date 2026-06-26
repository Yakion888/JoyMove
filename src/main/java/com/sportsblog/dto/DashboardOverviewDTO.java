package com.sportsblog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 看板总览 DTO
 */
@Data
public class DashboardOverviewDTO {

    /** KPI 统计 */
    private WeeklyStatsDTO weeklyStats;

    /** 运动项目分布 */
    private List<ProjectDistributionDTO> projectDistribution;

    /** 连续打卡信息 */
    private StreakInfoDTO streakInfo;

    /** 勋章汇总 */
    private MedalSummaryDTO medalSummary;
}
