# CLAUDE.md — 悦动宝 (JoyMove) 项目规范

## 项目概述

Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Thymeleaf + jQuery + Bootstrap 5 亲子运动陪伴平台。

---

## Bug 修复规范

### 每次修复 bug 时必须执行：

1. **使用报告模板**：基于 `.claude/bugfix-report-template.md` 模板填写
2. **报告存放位置**：`.claude/bugfixes/YYYY-MM-DD-简短描述.md`
3. **报告包含**：问题描述 → 复现步骤 → 根因分析 → 修复方案 → 验证结果 → 经验教训

### Bug 排查原则：

- 先确认是代码逻辑 bug 还是数据边界问题（KPI 为 0 不一定等于 bug）
- 对比同类方法的实现（如 `getStreakInfo()` vs `calcCurrentStreak()`），检查是否一致
- 检查 SQL 过滤条件是否与其他相关查询一致
- 检查日期/时间边界条件

### 查询规范：

- 内部聚合计算使用无分页的 List 查询，不使用 MyBatis-Plus Page 对象
- 看板相关 SQL 统一使用 `status = 1` 过滤已发布记录
- 避免硬编码上限（如 Page(1, 1000)），除非是面向用户的分页列表

---

## 关键文件索引

| 模块 | 路径 |
|------|------|
| 看板服务 | `service/impl/DashboardServiceImpl.java` |
| 打卡服务 | `service/impl/FamilyMomentServiceImpl.java` |
| Mapper XML | `resources/mapper/FamilyMomentMapper.xml` |
| 看板前端 | `resources/templates/dashboard.html` |
| 打卡前端 | `resources/templates/checkin.html` |
| Bug 报告模板 | `.claude/bugfix-report-template.md` |
| Bug 报告存档 | `.claude/bugfixes/` |

---

## 运行方式

```bash
cd c:\Users\19429\Desktop\ai编程\悦动宝
mvn spring-boot:run
```

测试账号：`mama` / `admin123`
