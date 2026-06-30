package com.joymove.service;

import com.joymove.dto.MedalProgressDTO;
import com.joymove.entity.Medal;
import com.joymove.entity.MedalRecord;

import java.util.List;

public interface MedalService {

    /** 获取全部勋章定义 */
    List<Medal> getAll();

    /** 获取用户已获得的勋章记录 */
    List<MedalRecord> getUserRecords(Long userId);

    /** 获取勋章进度（含已获/未获+进度） */
    List<MedalProgressDTO> getProgress(Long userId);

    /** 检查并授予勋章（活动保存/打卡后触发） */
    void checkAndAward(Long userId);
}
