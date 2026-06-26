package com.sportsblog.service.impl;

import com.sportsblog.dto.MedalProgressDTO;
import com.sportsblog.entity.Medal;
import com.sportsblog.entity.MedalRecord;
import com.sportsblog.mapper.FamilyMomentMapper;
import com.sportsblog.mapper.MedalMapper;
import com.sportsblog.mapper.MedalRecordMapper;
import com.sportsblog.service.MedalService;
import com.sportsblog.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedalServiceImpl implements MedalService {

    @Autowired
    private MedalMapper medalMapper;

    @Autowired
    private MedalRecordMapper recordMapper;

    @Autowired
    private FamilyMomentMapper momentMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Medal> getAll() {
        return medalMapper.selectList(null);
    }

    @Override
    public List<MedalRecord> getUserRecords(Long userId) {
        List<Long> medalIds = recordMapper.selectMedalIdsByUserId(userId);
        return medalIds.stream().map(mid -> {
            MedalRecord mr = new MedalRecord();
            mr.setUserId(userId);
            mr.setMedalId(mid);
            return mr;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MedalProgressDTO> getProgress(Long userId) {
        List<Medal> all = getAll();
        Set<Long> earnedIds = recordMapper.selectMedalIdsByUserId(userId).stream().collect(Collectors.toSet());
        int total = momentMapper.countByUserId(userId);

        List<MedalProgressDTO> result = new ArrayList<>();
        for (Medal medal : all) {
            MedalProgressDTO dto = new MedalProgressDTO();
            copyFields(medal, dto);
            boolean earned = earnedIds.contains(medal.getId());
            int progress = calcProgress(medal, userId, total);
            int percent = Math.min(100, progress * 100 / Math.max(1, medal.getConditionValue()));

            // 进度满但未记录 → 自动补授
            if (percent >= 100 && !earned) {
                MedalRecord mr = new MedalRecord();
                mr.setUserId(userId); mr.setMedalId(medal.getId());
                mr.setEarnedDate(LocalDate.now());
                recordMapper.insert(mr);
                earned = true;
            }

            dto.setEarned(earned);
            dto.setCurrentProgress(progress);
            dto.setProgressPercent(percent);
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<MedalRecord> checkAndAward(Long userId) {
        List<Medal> all = getAll();
        Set<Long> earnedIds = recordMapper.selectMedalIdsByUserId(userId).stream().collect(Collectors.toSet());

        int total = momentMapper.countByUserId(userId);
        int variety = momentMapper.countDistinctProjects(userId);

        List<MedalRecord> newRecords = new ArrayList<>();
        for (Medal medal : all) {
            if (earnedIds.contains(medal.getId())) continue;
            if (evaluate(medal, userId, total, variety)) {
                MedalRecord mr = new MedalRecord();
                mr.setUserId(userId);
                mr.setMedalId(medal.getId());
                mr.setEarnedDate(LocalDate.now());
                recordMapper.insert(mr);
                newRecords.add(mr);

                notificationService.create(userId, 2,
                        "🎉 恭喜获得勋章：「" + medal.getMedalName() + "」— " + medal.getTriggerCondition(),
                        medal.getId());
            }
        }
        return newRecords;
    }

    private boolean evaluate(Medal medal, Long userId, int total, int variety) {
        switch (medal.getConditionType()) {
            case "activity_count":
                return total >= medal.getConditionValue();
            case "category_variety":
                return variety >= medal.getConditionValue();
            case "emotion_count":
                return momentMapper.countByEmotion(userId, 1) >= medal.getConditionValue();
            default:
                return false;
        }
    }

    private int calcProgress(Medal medal, Long userId, int total) {
        switch (medal.getConditionType()) {
            case "activity_count":
            case "emotion_count":
                return Math.min(total, medal.getConditionValue());
            case "category_variety":
                return Math.min(momentMapper.countDistinctProjects(userId), medal.getConditionValue());
            default:
                return 0;
        }
    }

    private void copyFields(Medal src, MedalProgressDTO dst) {
        dst.setId(src.getId()); dst.setMedalCode(src.getMedalCode());
        dst.setMedalName(src.getMedalName()); dst.setMedalIcon(src.getMedalIcon());
        dst.setTriggerCondition(src.getTriggerCondition());
        dst.setConditionType(src.getConditionType());
        dst.setConditionValue(src.getConditionValue());
    }
}
