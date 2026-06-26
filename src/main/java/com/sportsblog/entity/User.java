package com.sportsblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表 (user)
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** BCrypt 加密密码 */
    private String password;

    /** 头像路径 */
    private String avatar;

    /** 角色：0-家长，1-管理员 */
    private Integer role;

    /** 手机号 */
    private String phone;

    /** 所在城市 */
    private String city;

    /** 个人简介 */
    private String bio;

    /** 家庭角色：0-爸爸，1-妈妈，2-其他家人 */
    private Integer familyRole;

    /** 首次注册日期 */
    private LocalDate joinDate;

    /** 累计运动天数（冗余，加速看板） */
    private Integer totalDays;

    /** 最长连续打卡天数（冗余） */
    private Integer longestStreak;

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
