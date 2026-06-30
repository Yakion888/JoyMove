package com.joymove.dto;

import lombok.Data;

import java.util.List;

/**
 * 勋章汇总 DTO
 */
@Data
public class MedalSummaryDTO {

    /** 已获勋章数 */
    private Integer earnedCount;

    /** 总勋章数 */
    private Integer totalCount;

    /** 勋章进度列表 */
    private List<MedalProgressDTO> medals;
}
