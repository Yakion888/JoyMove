package com.joymove.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 孩子信息表 (child_profile)
 */
@Data
@TableName("child_profile")
public class ChildProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联家长用户 ID */
    private Long userId;

    /** 孩子姓名/昵称 */
    private String name;

    /** 性别：0-男，1-女 */
    private Integer gender;

    /** 出生日期（用于计算年龄） */
    private LocalDate birthDate;

    /** 头像路径 */
    private String avatar;

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
