package com.joymove.dto;

import com.joymove.entity.Medal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 勋章进度 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedalProgressDTO extends Medal {

    /** 是否已获得 */
    private Boolean earned;

    /** 当前进度值 */
    private Integer currentProgress;

    /** 进度百分比 0-100 */
    private Integer progressPercent;
}
