package com.joymove.service.impl;

import com.joymove.common.BusinessException;
import com.joymove.dto.CheckInCalendarDTO;
import com.joymove.entity.CheckIn;
import com.joymove.mapper.CheckInMapper;
import com.joymove.service.CheckInService;
import com.joymove.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private MedalService medalService;

    @Override
    @Transactional
    public CheckIn checkIn(Long userId, Long childId, Long momentId) {
        LocalDate today = LocalDate.now();

        CheckIn existing = checkInMapper.findByUserChildAndDate(userId, childId, today);
        if (existing != null) {
            throw new BusinessException("今天已经打卡过了，明天再来吧！");
        }

        LocalDate yesterday = today.minusDays(1);
        CheckIn yesterdayCheckIn = checkInMapper.findByUserChildAndDate(userId, childId, yesterday);
        int streakDays = (yesterdayCheckIn != null) ? yesterdayCheckIn.getStreakDays() + 1 : 1;

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setChildId(childId);
        checkIn.setCheckInDate(today);
        checkIn.setMomentId(momentId);
        checkIn.setStreakDays(streakDays);
        checkInMapper.insert(checkIn);

        medalService.checkAndAward(userId);
        log.info("Check-in successful: userId={}, childId={}, streakDays={}", userId, childId, streakDays);

        return checkIn;
    }

    @Override
    public Integer getCurrentStreak(Long userId, Long childId) {
        return checkInMapper.getCurrentStreak(userId, childId);
    }

    @Override
    public CheckInCalendarDTO getMonthlyCalendar(Long userId, Long childId, int year, int month) {
        List<CheckIn> checkIns = checkInMapper.selectMonthly(userId, childId, year, month);
        Set<LocalDate> dates = new HashSet<>();
        for (CheckIn ci : checkIns) dates.add(ci.getCheckInDate());

        CheckInCalendarDTO dto = new CheckInCalendarDTO();
        dto.setYear(year);
        dto.setMonth(month);
        dto.setCurrentStreak(getCurrentStreak(userId, childId));
        dto.setCheckInDates(dates);
        dto.setMonthlyCount(checkIns.size());
        return dto;
    }
}
