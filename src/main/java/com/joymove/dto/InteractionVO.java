package com.joymove.dto;

import com.joymove.entity.CommunityInteraction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 社区互动 VO（含用户昵称 + 树形子节点）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InteractionVO extends CommunityInteraction {

    /** 互动者昵称 */
    private String nickname;

    /** 互动者头像 */
    private String avatar;

    /** 子互动列表 */
    private List<InteractionVO> children = new ArrayList<>();

    /** 嵌套深度 */
    private Integer depth;
}
