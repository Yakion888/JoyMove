package com.sportsblog.dto;

import lombok.Data;

/**
 * 本周运动统计 DTO
 */
@Data
public class WeeklyStatsDTO {

    /** 本周运动次数 */
    private Integer weeklyCount;

    /** 本周运动时长（分钟） */
    private Integer weeklyDuration;

    /** 本月运动次数 */
    private Integer monthlyCount;

    /** 本月运动时长（分钟） */
    private Integer monthlyDuration;

    /** 累计运动次数 */
    private Integer totalCount;

    /** 累计运动时长（分钟） */
    private Integer totalDuration;

    /** 平均心情评分 */
    private Double avgEmotion;

    /** 平均星级 */
    private Double avgStars;
}
