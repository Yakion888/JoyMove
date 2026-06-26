package com.sportsblog.dto;

import com.sportsblog.entity.FamilyMoment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运动记录详情 VO（含关联信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MomentDetailVO extends FamilyMoment {

    /** 作者昵称 */
    private String authorNickname;

    /** 作者头像 */
    private String authorAvatar;

    /** 孩子名称 */
    private String childName;

    /** 项目名称 */
    private String projectName;
}
