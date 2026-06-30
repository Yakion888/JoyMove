package com.joymove.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知表 (notification)
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 ID */
    private Long userId;

    /** 通知类型：1-活动驳回，2-勋章获得，3-打卡提醒，4-新互动 */
    private Integer type;

    /** 消息内容 */
    private String message;

    /** 关联业务 ID */
    private Long relatedId;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
