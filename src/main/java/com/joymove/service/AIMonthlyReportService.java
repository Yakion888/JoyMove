package com.joymove.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joymove.dto.*;
import com.joymove.entity.*;
import com.joymove.mapper.ChildProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;

/**
 * AI 月度成长报告服务
 */
@Slf4j
@Service
public class AIMonthlyReportService {

    @Autowired
    private AIClientService aiClient;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ChildProfileMapper childProfileMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "aiReport", key = "#childId + ':' + #year + ':' + #month")
    public Map<String, Object> generateReport(Long userId, Long childId, int year, int month) {
        ChildProfile child = childProfileMapper.selectById(childId);
        if (child == null) return Collections.emptyMap();

        int age = YearMonth.now().getYear() - child.getBirthDate().getYear();
        WeeklyStatsDTO stats = dashboardService.getWeeklyStats(userId);
        StreakInfoDTO streak = dashboardService.getStreakInfo(userId);
        MedalSummaryDTO medals = dashboardService.getMedalSummary(userId);
        MonthlyTrendDTO trend = dashboardService.getMonthlyTrend(userId, 3);
        List<ProjectDistributionDTO> distribution = dashboardService.getProjectDistribution(userId);

        int monthCount = 0, monthDuration = 0;
        if (trend.getMonths() != null) {
            String ym = String.format("%d-%02d", year, month);
            for (int i = 0; i < trend.getMonths().size(); i++) {
                if (ym.equals(trend.getMonths().get(i))) {
                    monthCount = trend.getCounts().get(i);
                    monthDuration = trend.getDurations().get(i);
                    break;
                }
            }
        }

        String topProject = distribution.isEmpty() ? "无" :
            distribution.get(0).getProjectName() + "(" + distribution.get(0).getCount() + "次)";

        double avgE = stats.getAvgEmotion() != null ? stats.getAvgEmotion() : 0;
        double avgS = stats.getAvgStars() != null ? stats.getAvgStars() : 0;

        String systemPrompt = "你是一个温暖的亲子运动顾问。根据孩子的运动数据生成一份充满鼓励的月度成长报告。严格返回JSON格式。";
        String userMessage = "孩子：" + child.getName() + "，" + age + "岁。" + year + "年" + month + "月运动数据：\n" +
            "- 本月运动" + monthCount + "次，累计" + monthDuration + "分钟\n" +
            "- 平均心情" + avgE + "分(1最开心)，平均自评" + avgS + "星\n" +
            "- 最爱运动：" + topProject + "\n" +
            "- 连续打卡" + streak.getCurrentStreak() + "天，最长纪录" + streak.getLongestStreak() + "天\n" +
            "- 已获勋章" + medals.getEarnedCount() + "/" + medals.getTotalCount() + "枚\n" +
            "- 累计运动" + streak.getTotalDays() + "次，总共" + stats.getTotalDuration() + "分钟\n\n" +
            "请返回JSON：{\"title\":\"报告标题(15字内)\",\"summary\":\"温暖总结(100字内)\"," +
            "\"highlights\":[\"亮点1\",\"亮点2\",\"亮点3\"],\"encouragement\":\"鼓励语(50字内)\"," +
            "\"nextGoal\":\"下月小目标(30字内)\"}";

        try {
            String aiResponse = aiClient.chat(systemPrompt, userMessage);
            if (aiResponse != null) {
                Map<String, Object> report = objectMapper.readValue(aiResponse,
                    new TypeReference<Map<String, Object>>() {});
                report.put("childName", child.getName());
                report.put("childAge", age);
                report.put("year", year); report.put("month", month);
                report.put("monthCount", monthCount);
                report.put("monthDuration", monthDuration);
                report.put("medalCount", medals.getEarnedCount());
                report.put("streakDays", streak.getCurrentStreak());
                log.info("AI月度报告生成成功");
                return report;
            }
        } catch (Exception e) {
            log.warn("AI报告生成失败: {}", e.getMessage());
        }

        // fallback
        Map<String, Object> fb = new LinkedHashMap<>();
        fb.put("childName", child.getName()); fb.put("childAge", age);
        fb.put("year", year); fb.put("month", month);
        fb.put("title", child.getName() + "的" + month + "月运动报告");
        fb.put("summary", "本月完成" + monthCount + "次运动，累计" + monthDuration + "分钟，表现很棒！");
        fb.put("highlights", Arrays.asList("完成" + monthCount + "次亲子运动", "最爱" + topProject, "连续打卡" + streak.getCurrentStreak() + "天"));
        fb.put("encouragement", "每一次运动都是成长的印记，和孩子一起继续前行！");
        fb.put("nextGoal", "下个月争取运动" + (monthCount + 2) + "次");
        fb.put("monthCount", monthCount); fb.put("monthDuration", monthDuration);
        fb.put("medalCount", medals.getEarnedCount()); fb.put("streakDays", streak.getCurrentStreak());
        return fb;
    }
}
