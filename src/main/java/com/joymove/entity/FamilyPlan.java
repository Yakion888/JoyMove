package com.joymove.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户运动计划表 (family_plan)
 */
@Data
@TableName("family_plan")
public class FamilyPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 关联运动项目 ID */
    private Long projectId;

    /** 计划日期 */
    private LocalDate plannedDate;

    /** 状态：0-待执行，1-已完成，2-已跳过 */
    private Integer status;

    /** 实际时长（完成后填写） */
    private Integer actualDuration;

    /** 孩子反馈（完成后填写） */
    private String childFeedback;

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
