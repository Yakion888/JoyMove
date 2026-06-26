package com.sportsblog.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

/**
 * 打卡日历 DTO
 */
@Data
public class CheckInCalendarDTO {

    /** 年份 */
    private Integer year;

    /** 月份 */
    private Integer month;

    /** 当前连续天数 */
    private Integer currentStreak;

    /** 本月打卡日期集合 */
    private Set<LocalDate> checkInDates;

    /** 本月打卡天数 */
    private Integer monthlyCount;
}
