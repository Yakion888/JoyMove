package com.sportsblog.dto;

import lombok.Data;

import java.util.List;

/**
 * 月度趋势 DTO（折线图数据）
 */
@Data
public class MonthlyTrendDTO {

    /** 月份标签 */
    private List<String> months;

    /** 各月运动次数 */
    private List<Integer> counts;

    /** 各月运动时长 */
    private List<Integer> durations;
}
