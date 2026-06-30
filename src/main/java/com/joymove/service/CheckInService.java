package com.joymove.service;

import com.joymove.dto.CheckInCalendarDTO;
import com.joymove.entity.CheckIn;

public interface CheckInService {

    /** 打卡 */
    CheckIn checkIn(Long userId, Long childId, Long momentId);

    /** 当前连续天数 */
    Integer getCurrentStreak(Long userId, Long childId);

    /** 月度日历 */
    CheckInCalendarDTO getMonthlyCalendar(Long userId, Long childId, int year, int month);
}
