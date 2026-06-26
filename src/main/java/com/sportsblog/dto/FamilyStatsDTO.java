package com.sportsblog.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 家庭运动数据看板 DTO
 */
@Data
public class FamilyStatsDTO {

    /** 累计运动天数 */
    private Integer totalDays;

    /** 总活动次数 */
    private Integer totalActivities;

    /** 总运动时长（分钟） */
    private Integer totalDuration;

    /** 平均心情评分 */
    private BigDecimal avgEmotion;

    /** 当前连续打卡天数 */
    private Integer currentStreak;

    /** 最长连续打卡天数 */
    private Integer longestStreak;

    /** 体验过的运动项目数 */
    private Integer projectVariety;

    /** 最常参与的项目名称 */
    private String favoriteProject;

    /** 已获勋章数 */
    private Integer medalCount;
}
