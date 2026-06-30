package com.joymove.service.impl;

import com.joymove.dto.InteractionVO;
import com.joymove.entity.CommunityInteraction;
import com.joymove.entity.User;
import com.joymove.mapper.CommunityInteractionMapper;
import com.joymove.mapper.FamilyMomentMapper;
import com.joymove.mapper.UserMapper;
import com.joymove.service.CommunityInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommunityInteractionServiceImpl implements CommunityInteractionService {

    @Autowired
    private CommunityInteractionMapper interactionMapper;

    @Autowired
    private FamilyMomentMapper momentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public CommunityInteraction save(Long momentId, String content, Long parentId, Long userId) {
        CommunityInteraction interaction = new CommunityInteraction();
        interaction.setMomentId(momentId);
        interaction.setContent(content);
        interaction.setParentId(parentId != null ? parentId : 0L);
        interaction.setUserId(userId);
        interaction.setLikeCount(0);
        interactionMapper.insert(interaction);

        // 更新运动记录的评论数
        momentMapper.incrementCommentCount(momentId);
        log.info("Community interaction saved: id={}, momentId={}, userId={}", interaction.getId(), momentId, userId);

        return interaction;
    }

    @Override
    public List<InteractionVO> getTreeFlat(Long momentId) {
        List<CommunityInteraction> all = interactionMapper.selectByMomentId(momentId);
        List<InteractionVO> vos = new ArrayList<>();
        for (CommunityInteraction ci : all) {
            vos.add(toVO(ci));
        }
        List<InteractionVO> roots = buildTree(vos);
        List<InteractionVO> flat = new ArrayList<>();
        flatten(roots, flat, 0);
        return flat;
    }

    private List<InteractionVO> buildTree(List<InteractionVO> all) {
        Map<Long, InteractionVO> map = new HashMap<>();
        List<InteractionVO> roots = new ArrayList<>();
        for (InteractionVO vo : all) map.put(vo.getId(), vo);
        for (InteractionVO vo : all) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                InteractionVO parent = map.get(vo.getParentId());
                if (parent != null) parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    private void flatten(List<InteractionVO> nodes, List<InteractionVO> result, int depth) {
        for (InteractionVO node : nodes) {
            node.setDepth(depth);
            result.add(node);
            if (!node.getChildren().isEmpty()) flatten(node.getChildren(), result, depth + 1);
        }
    }

    private InteractionVO toVO(CommunityInteraction ci) {
        InteractionVO vo = new InteractionVO();
        vo.setId(ci.getId()); vo.setMomentId(ci.getMomentId());
        vo.setUserId(ci.getUserId()); vo.setParentId(ci.getParentId());
        vo.setContent(ci.getContent()); vo.setLikeCount(ci.getLikeCount());
        vo.setCreateTime(ci.getCreateTime());
        if (ci.getUserId() != null) {
            User user = userMapper.selectById(ci.getUserId());
            if (user != null) { vo.setNickname(user.getNickname()); vo.setAvatar(user.getAvatar()); }
        }
        return vo;
    }
}
