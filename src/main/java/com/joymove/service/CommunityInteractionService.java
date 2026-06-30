package com.joymove.service;

import com.joymove.dto.InteractionVO;
import com.joymove.entity.CommunityInteraction;

import java.util.List;

public interface CommunityInteractionService {

    /** 发布互动 */
    CommunityInteraction save(Long momentId, String content, Long parentId, Long userId);

    /** 获取互动树（扁平化） */
    List<InteractionVO> getTreeFlat(Long momentId);
}
