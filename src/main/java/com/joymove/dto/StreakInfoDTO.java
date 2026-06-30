package com.joymove.dto;

import lombok.Data;

/**
 * 连续打卡信息 DTO
 */
@Data
public class StreakInfoDTO {

    /** 当前连续天数 */
    private Integer currentStreak;

    /** 最长连续天数 */
    private Integer longestStreak;

    /** 本月打卡天数 */
    private Integer monthlyCheckDays;

    /** 总运动天数 */
    private Integer totalDays;
}
