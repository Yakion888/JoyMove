package com.joymove.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.entity.ChildProfile;
import com.joymove.entity.FamilyPlan;
import com.joymove.entity.SportProject;
import com.joymove.mapper.ChildProfileMapper;
import com.joymove.mapper.FamilyPlanMapper;
import com.joymove.mapper.SportProjectMapper;
import com.joymove.service.FamilyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FamilyPlanServiceImpl implements FamilyPlanService {

    @Autowired
    private FamilyPlanMapper planMapper;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private ChildProfileMapper childProfileMapper;

    @Override
    public FamilyPlan create(FamilyPlan plan) {
        plan.setStatus(0);
        planMapper.insert(plan);
        log.info("FamilyPlan created: id={}, userId={}, projectId={}", plan.getId(), plan.getUserId(), plan.getProjectId());
        return plan;
    }

    @Override
    public void complete(Long id, Integer actualDuration, String childFeedback) {
        FamilyPlan plan = planMapper.selectById(id);
        if (plan != null) {
            plan.setStatus(1);
            plan.setActualDuration(actualDuration);
            plan.setChildFeedback(childFeedback);
            planMapper.updateById(plan);
            log.info("FamilyPlan completed: id={}, actualDuration={}", id, actualDuration);
        }
    }

    @Override
    public void skip(Long id) {
        FamilyPlan plan = planMapper.selectById(id);
        if (plan != null) {
            plan.setStatus(2);
            planMapper.updateById(plan);
        }
    }

    @Override
    public List<FamilyPlan> getByDateRange(Long userId, String startDate, String endDate) {
        return planMapper.selectByDateRange(userId, startDate, endDate);
    }

    @Override
    public List<FamilyPlan> getByUserId(Long userId) {
        LambdaQueryWrapper<FamilyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyPlan::getUserId, userId)
               .orderByDesc(FamilyPlan::getPlannedDate);
        return planMapper.selectList(wrapper);
    }

    @Override
    public List<FamilyPlan> recommend(Long childId, int count) {
        ChildProfile child = childProfileMapper.selectById(childId);
        if (child == null || child.getBirthDate() == null) return Collections.emptyList();
        int age = Period.between(child.getBirthDate(), LocalDate.now()).getYears();
        String season = getCurrentSeason();

        List<SportProject> projects = projectMapper.selectList(
            new LambdaQueryWrapper<SportProject>().eq(SportProject::getStatus, 1));
        List<ScoredPlan> scored = new ArrayList<>();
        for (SportProject p : projects) {
            if (age < p.getAgeRangeMin() || age > p.getAgeRangeMax()) continue;
            int s = 10;
            if (p.getSeasonRecommend() != null) {
                int seasonCode = getSeasonCode(season);
                if (p.getSeasonRecommend() == 5 || p.getSeasonRecommend() == seasonCode) s += 5;
            }
            scored.add(new ScoredPlan(p.getId(), s));
        }
        scored.sort((a, b) -> b.score - a.score);
        return scored.stream().limit(count).map(sp -> {
            FamilyPlan fp = new FamilyPlan();
            fp.setProjectId(sp.projectId);
            return fp;
        }).collect(Collectors.toList());
    }

    private String getCurrentSeason() {
        int m = LocalDate.now().getMonthValue();
        if (m >= 3 && m <= 5) return "spring"; if (m >= 6 && m <= 8) return "summer";
        if (m >= 9 && m <= 11) return "autumn"; return "winter";
    }
    private int getSeasonCode(String s) {
        switch (s) { case "spring": return 1; case "summer": return 2; case "autumn": return 3; case "winter": return 4; default: return 5; }
    }
    private static class ScoredPlan { Long projectId; int score; ScoredPlan(Long id, int s) { this.projectId = id; this.score = s; } }
}
