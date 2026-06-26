package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运动项目模板库 (sport_project)
 */
@Data
@TableName("sport_project")
public class SportProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String name;

    /** 最小推荐年龄 */
    private Integer ageRangeMin;

    /** 最大推荐年龄 */
    private Integer ageRangeMax;

    /** 最短时长（分钟） */
    private Integer durationMin;

    /** 最长时长（分钟） */
    private Integer durationMax;

    /** 所需道具 */
    private String equipment;

    /** 能力培养标签（逗号分隔） */
    private String abilityTags;

    /** 详细玩法描述 */
    private String description;

    /** 难度等级 1-5 */
    private Integer difficultyLevel;

    /** 运动类型：1-户外，2-室内，3-两者皆可 */
    private Integer activityType;

    /** 季节推荐：1-春，2-夏，3-秋，4-冬，5-全年 */
    private Integer seasonRecommend;

    /** 封面图路径 */
    private String coverImage;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

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
