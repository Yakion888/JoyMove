package com.joymove.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joymove.entity.MomentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {

    /** 检查用户是否已点赞 */
    int existsByUserAndMoment(@Param("userId") Long userId, @Param("momentId") Long momentId);
}
