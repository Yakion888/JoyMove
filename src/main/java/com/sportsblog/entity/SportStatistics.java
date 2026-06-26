package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动统计缓存表 (sport_statistics)
 */
@Data
@TableName("sport_statistics")
public class SportStatistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 统计日期 */
    private LocalDate statDate;

    /** 当天活动次数 */
    private Integer activityCount;

    /** 当天运动时长（分钟） */
    private Integer totalDuration;

    /** 当天平均心情 */
    private BigDecimal avgEmotion;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
