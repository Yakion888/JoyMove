# Bug 修复报告：运动数据看板分页查询优化

**报告日期**：2026-06-29
**报告人**：开发排查
**严重程度**：低
**状态**：已修复

---

## 1. 问题描述

**现象**：  
使用 `mama` / `admin123` 账号登录，进入「运动数据看板」页面，三个 KPI（本周运动次数、本周运动时长、连续打卡天数）均显示为 0。打卡面板的日历视图中可以看到当月的打卡记录。

**影响范围**：  
数据看板 `DashboardServiceImpl` 中的 `getWeeklyStats()` 和 `getProjectDistribution()` 方法。

---

## 2. 复现步骤

1. 使用 `mama` / `admin123` 登录
2. 确认打卡面板有本月打卡记录（例如 6 月 22-28 日有打卡）
3. 进入「运动数据看板」页面
4. 观察 KPI 卡片

**预期结果**：KPI 显示正确的统计数据
**实际结果**：三个 KPI 均为 0

**排查结论**：三个 KPI 为 0 **不是代码逻辑 bug**，而是数据边界导致：
- 今日为周一（6月29日），`weekStart = today.with(DayOfWeek.MONDAY) = 6月29日`，上周打卡均被排除 → 本周统计为 0（正确）
- 最近打卡距今超过 1 天（例如周五打卡后周末中断），连续打卡中断 → streak = 0（正确）

---

## 3. 根因分析

虽不是直接导致 0 的原因，但 `DashboardServiceImpl` 中存在查询隐患：

**问题代码位置**：  
`DashboardServiceImpl.java` — `getWeeklyStats()` (line 50) 和 `getProjectDistribution()` (line 86)

```java
// 原代码
List<FamilyMoment> all = momentMapper.selectByUserIdPage(
    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 1000), userId).getRecords();
```

**隐患**：
1. **硬上限 1000 条** — 用户数据超过 1000 条会被静默截断
2. **无 `status = 1` 过滤** — 与其他看板查询（日历、趋势、连续打卡的 SQL 均有 `status = 1`）不一致
3. **重复查询** — 每次 `getOverview()` 请求调用了两次相同的分页查询（weeklyStats + projectDistribution）

---

## 4. 修复方案

**修改文件**：

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| `mapper/FamilyMomentMapper.java` | 新增方法 | 新增 `selectAllByUserId` 声明，无分页、带 status=1 |
| `mapper/FamilyMomentMapper.xml` | 新增 SQL | `WHERE user_id = ? AND is_deleted = 0 AND status = 1` |
| `service/impl/DashboardServiceImpl.java` | 修改 | `getWeeklyStats()` 和 `getProjectDistribution()` 改用 `selectAllByUserId` |

**修复前/后对比**：

```diff
- List<FamilyMoment> all = momentMapper.selectByUserIdPage(
-     new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 1000), userId).getRecords();
+ List<FamilyMoment> all = momentMapper.selectAllByUserId(userId);
```

新 SQL：
```sql
SELECT * FROM family_moment
WHERE user_id = #{userId} AND is_deleted = 0 AND status = 1
ORDER BY record_date DESC
```

---

## 5. 验证

**验证环境**：本地开发

**验证步骤**：
1. 启动应用，`mama` / `admin123` 登录
2. 进入数据看板，确认页面正常加载
3. KPI 数值与修改前一致（本次只改查询方式，不改计算逻辑）
4. 总运动次数/时长正确累加
5. 项目分布饼图数据正确

**回归检查**：
- [x] 打卡面板日历功能正常
- [x] 月度趋势图数据正确
- [x] 成长时间线正常展示

---

## 6. 经验教训

1. **查询一致性**：看板相关的所有查询应统一使用 `status = 1` 过滤，避免不同视图看到不同数据
2. **分页误用**：内部聚合计算不应使用分页查询（Page 对象），应使用无分页的 List 查询
3. **KPI 为 0 不一定等于 bug**：先确认是代码逻辑问题还是数据边界问题，避免误改正确的业务逻辑
