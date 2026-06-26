package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运动记录点赞表 (moment_like)
 */
@Data
@TableName("moment_like")
public class MomentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 运动记录 ID */
    private Long momentId;

    /** 点赞用户 ID */
    private Long userId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
