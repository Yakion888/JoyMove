package com.sportsblog.dto;

import lombok.Data;

import java.util.List;

/**
 * 月度运动日历 DTO
 */
@Data
public class MonthlyCalendarDTO {

    private Integer year;
    private Integer month;
    private List<CalendarDay> days;
    private Integer totalDays;      // 本月运动天数
    private Integer streakDays;     // 截至本月最后一天的连续天数
    private Integer totalActivities; // 累计运动总次数（全部历史）

    @Data
    public static class CalendarDay {
        private String date;         // "2026-06-15"
        private Boolean hasRecord;
        private String projectName;  // 当天运动项目名（多项目取第一个）
        private String emotionEmoji; // 😄😊😐😓
        private Integer stars;       // 孩子自评
    }
}
