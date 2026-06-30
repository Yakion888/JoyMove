package com.joymove.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joymove.entity.*;
import com.joymove.mapper.ChildProfileMapper;
import com.joymove.mapper.SportProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 运动推荐服务
 */
@Slf4j
@Service
public class AIRecommendationService {

    @Autowired
    private AIClientService aiClient;

    @Autowired
    private SportProjectMapper projectMapper;

    @Autowired
    private ChildProfileMapper childProfileMapper;

    @Autowired
    private FamilyPlanService planService; // 复用规则引擎做 fallback

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI 推荐运动项目
     * @param childId 孩子ID
     * @param weather 天气描述（如 "晴天 28°C"）
     * @param count 推荐数量
     */
    public List<Map<String, Object>> recommend(Long childId, String weather, int count) {
        ChildProfile child = childProfileMapper.selectById(childId);
        if (child == null) return Collections.emptyList();

        int age = LocalDate.now().getYear() - child.getBirthDate().getYear();
        String gender = child.getGender() != null && child.getGender() == 1 ? "女孩" : "男孩";
        String season = getCurrentSeason();

        // 获取所有可用项目
        List<SportProject> allProjects = projectMapper.selectList(null);
        String projectList = allProjects.stream()
                .map(p -> String.format("%s(ID:%d,年龄%d-%d,难度%d,%s,装备:%s,标签:%s)",
                    p.getName(), p.getId(), p.getAgeRangeMin(), p.getAgeRangeMax(),
                    p.getDifficultyLevel(),
                    p.getActivityType() == 1 ? "户外" : p.getActivityType() == 2 ? "室内" : "皆可",
                    p.getEquipment() != null ? p.getEquipment() : "无",
                    p.getAbilityTags() != null ? p.getAbilityTags() : ""))
                .collect(Collectors.joining("\n"));

        // 构建 prompt
        String systemPrompt = "你是一个专业的亲子运动规划师。根据孩子的年龄、性别、季节、天气和可用运动项目列表，推荐最合适的亲子运动项目。请严格返回JSON数组格式：";
        String userMessage = String.format(
            "孩子信息：%d岁%s。当前季节：%s。天气：%s。\n\n可用运动项目列表：\n%s\n\n请从以上列表中推荐%d个最适合的运动项目，返回格式如下JSON：\n" +
            "{\"recommendations\":[{\"projectId\":数字,\"reason\":\"推荐理由(30字内)\",\"tips\":\"运动小贴士(30字内)\"}]}",
            age, gender, season, weather, projectList, count);

        // 调用 AI
        try {
            String aiResponse = aiClient.chat(systemPrompt, userMessage);
            if (aiResponse != null) {
                Map<String, Object> result = objectMapper.readValue(aiResponse,
                    new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> recommendations = (List<Map<String, Object>>) result.get("recommendations");
                // 填充项目名称和图标
                for (Map<String, Object> r : recommendations) {
                    Object pidObj = r.get("projectId");
                    if (pidObj != null) {
                        Long pid = Long.valueOf(pidObj.toString());
                        SportProject proj = projectMapper.selectById(pid);
                        if (proj != null) {
                            r.put("projectName", proj.getName());
                            r.put("ageRange", proj.getAgeRangeMin() + "-" + proj.getAgeRangeMax() + "岁");
                            r.put("duration", proj.getDurationMin() + "-" + proj.getDurationMax() + "分钟");
                            r.put("equipment", proj.getEquipment());
                            r.put("activityType", proj.getActivityType());
                        }
                    }
                }
                log.info("AI推荐成功，返回{}个项目", recommendations.size());
                return recommendations;
            }
        } catch (Exception e) {
            log.warn("AI推荐解析失败，降级到规则引擎: {}", e.getMessage());
        }

        // fallback: 规则引擎
        log.info("使用规则引擎推荐");
        List<FamilyPlan> plans = planService.recommend(childId, count);
        return plans.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("projectId", p.getProjectId());
            m.put("reason", "根据年龄和季节智能匹配");
            SportProject proj = projectMapper.selectById(p.getProjectId());
            if (proj != null) {
                m.put("projectName", proj.getName());
                m.put("ageRange", proj.getAgeRangeMin() + "-" + proj.getAgeRangeMax() + "岁");
                m.put("duration", proj.getDurationMin() + "-" + proj.getDurationMax() + "分钟");
                m.put("equipment", proj.getEquipment());
            }
            return m;
        }).collect(Collectors.toList());
    }

    private String getCurrentSeason() {
        int m = LocalDate.now().getMonthValue();
        if (m >= 3 && m <= 5) return "春季";
        if (m >= 6 && m <= 8) return "夏季";
        if (m >= 9 && m <= 11) return "秋季";
        return "冬季";
    }
}
