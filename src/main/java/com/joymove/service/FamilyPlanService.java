package com.joymove.service;

import com.joymove.entity.FamilyPlan;

import java.util.List;

public interface FamilyPlanService {

    /** 创建计划 */
    FamilyPlan create(FamilyPlan plan);

    /** 完成计划 */
    void complete(Long id, Integer actualDuration, String childFeedback);

    /** 跳过计划 */
    void skip(Long id);

    /** 按日期范围查询 */
    List<FamilyPlan> getByDateRange(Long userId, String startDate, String endDate);

    /** 获取用户所有计划 */
    List<FamilyPlan> getByUserId(Long userId);

    /** 根据孩子推荐计划 */
    List<FamilyPlan> recommend(Long childId, int count);
}
