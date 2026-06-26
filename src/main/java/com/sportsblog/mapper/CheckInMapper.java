package com.sportsblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportsblog.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /** 查询用户在某日期的打卡 */
    CheckIn findByUserChildAndDate(@Param("userId") Long userId,
                                   @Param("childId") Long childId,
                                   @Param("checkInDate") LocalDate checkInDate);

    /** 获取当前连续天数 */
    Integer getCurrentStreak(@Param("userId") Long userId, @Param("childId") Long childId);

    /** 月度打卡日历 */
    List<CheckIn> selectMonthly(@Param("userId") Long userId,
                                @Param("childId") Long childId,
                                @Param("year") Integer year,
                                @Param("month") Integer month);
}
