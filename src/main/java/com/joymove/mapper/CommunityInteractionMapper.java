package com.joymove.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joymove.entity.CommunityInteraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityInteractionMapper extends BaseMapper<CommunityInteraction> {

    /** 查询运动记录下的所有互动 */
    List<CommunityInteraction> selectByMomentId(@Param("momentId") Long momentId);
}
