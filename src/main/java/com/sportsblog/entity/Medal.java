package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 勋章定义表 (medal)
 */
@Data
@TableName("medal")
public class Medal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 勋章编码，如 "NEW_SPROUT" */
    private String medalCode;

    /** 勋章名称，如 "运动新芽" */
    private String medalName;

    /** 勋章图标 (emoji) */
    private String medalIcon;

    /** 触发条件描述，如 "完成第1次运动打卡" */
    private String triggerCondition;

    /** 条件类型：activity_count / streak_days / category_variety */
    private String conditionType;

    /** 条件阈值 */
    private Integer conditionValue;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
