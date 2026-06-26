package com.sportsblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportsblog.entity.FamilyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyPlanMapper extends BaseMapper<FamilyPlan> {

    /** 按用户+日期范围查询 */
    List<FamilyPlan> selectByDateRange(@Param("userId") Long userId,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate);
}
