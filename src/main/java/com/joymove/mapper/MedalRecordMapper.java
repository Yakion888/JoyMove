package com.joymove.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.joymove.entity.MedalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedalRecordMapper extends BaseMapper<MedalRecord> {

    /** 查询用户已获得的勋章ID列表 */
    List<Long> selectMedalIdsByUserId(@Param("userId") Long userId);
}
