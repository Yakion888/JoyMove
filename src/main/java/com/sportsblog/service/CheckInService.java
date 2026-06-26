package com.sportsblog.service;

import com.sportsblog.dto.CheckInCalendarDTO;
import com.sportsblog.entity.CheckIn;

public interface CheckInService {

    /** 打卡 */
    CheckIn checkIn(Long userId, Long childId, Long momentId);

    /** 当前连续天数 */
    Integer getCurrentStreak(Long userId, Long childId);

    /** 月度日历 */
    CheckInCalendarDTO getMonthlyCalendar(Long userId, Long childId, int year, int month);
}
