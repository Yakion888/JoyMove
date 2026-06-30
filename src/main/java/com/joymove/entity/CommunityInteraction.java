package com.joymove.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区互动表 (community_interaction)
 */
@Data
@TableName("community_interaction")
public class CommunityInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联运动记录 ID */
    private Long momentId;

    /** 评论者 ID */
    private Long userId;

    /** 父评论 ID：0=根评论 */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 获赞数 */
    private Integer likeCount;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
