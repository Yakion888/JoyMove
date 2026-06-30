package com.joymove.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户勋章记录表 (medal_record)
 */
@Data
@TableName("medal_record")
public class MedalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 勋章 ID */
    private Long medalId;

    /** 获得日期 */
    private LocalDate earnedDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
