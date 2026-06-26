package com.sportsblog.dto;

import lombok.Data;

/**
 * 运动项目分布 DTO（饼图数据）
 */
@Data
public class ProjectDistributionDTO {

    /** 项目名称 */
    private String projectName;

    /** 项目图标 */
    private String projectIcon;

    /** 次数 */
    private Integer count;

    /** 占比百分比 */
    private Double percentage;
}
