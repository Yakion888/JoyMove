package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日打卡表 (check_in)
 */
@Data
@TableName("check_in")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 孩子 ID */
    private Long childId;

    /** 打卡日期 */
    private LocalDate checkInDate;

    /** 关联运动记录 ID（可为空） */
    private Long momentId;

    /** 连续打卡天数 */
    private Integer streakDays;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
