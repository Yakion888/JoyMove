package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 亲子运动记录表 (family_moment)
 */
@Data
@TableName("family_moment")
public class FamilyMoment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者 ID */
    private Long userId;

    /** 关联运动项目 ID */
    private Long projectId;

    /** 关联孩子 ID */
    private Long childId;

    /** 运动时孩子年龄（快照） */
    private Integer childAgeAtMoment;

    /** 实际运动时长（分钟） */
    private Integer duration;

    /** 运动地点 */
    private String location;

    /** 文字记录/感受 */
    private String content;

    /** 照片路径 */
    private String imageUrl;

    /** 心情：1-很开心，2-开心，3-一般，4-有点累 */
    private Integer emotion;

    /** 孩子自评星级 1-5 */
    private Integer stars;

    /** 状态：0-待审核，1-已发布，2-已驳回 */
    private Integer status;

    /** 驳回理由 */
    private String rejectReason;

    /** 话题标签（逗号分隔），如 "亲子跑,公园时光" */
    private String tags;

    /** 是否公开：0-仅自己，1-公开 */
    private Integer isPublic;

    /** 获赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 运动日期 */
    private LocalDate recordDate;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
