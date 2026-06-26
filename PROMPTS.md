# 🤖 AI 编程实践文档 — 悦动宝 JoyMove

> 本文档沉淀了使用 Claude Code 从零构建悦动宝的完整 AI 编程工作流、可复用提示词模板、项目知识库结构及操作指南。

---

## 一、项目方案与架构决策

### 1.1 背景

将一个已有的 Spring Boot 体育博客系统，在约 3 天内全面转型为亲子运动陪伴平台。原有代码包含 15 个 Thymeleaf 模板、6 个 Entity、8 个 Controller，但面向的是 NBA/足球等泛体育场景。

### 1.2 AI 辅助架构设计

**第一步：让 AI 理解现状**

```
请探索这个 Spring Boot 项目的完整结构，输出：
1. 所有 Entity 类及其字段
2. 所有 Controller 端点
3. Service 层调用链路
4. 前端模板清单
```

AI 在 30 秒内输出了完整的项目地图，人类需要手动翻阅 50+ 文件至少 20 分钟。

**第二步：让 AI 输出多方案对比**

```
我们需要将体育博客转型为亲子运动平台。请给出 3 种数据模型设计方案：
A. 最小改动（复用旧表，加字段）
B. 渐进式（新表 + 旧表共存，逐步迁移）
C. 彻底重建（全新 11 张表）
对比每种方案的工期、风险、扩展性。
```

AI 给出了带权重的对比表，我们选择了方案 C——干净切割、不留技术债务。

**第三步：让 AI 细化到表结构**

```
请输出 11 张新表的完整 DDL，字段需包含：
- sport_project: 运动项目模板库，含年龄范围/装备/能力标签/难度/季节
- family_moment: 运动记录，含情绪(1-4)/星级(1-5)/地点/连续天数
- medal + medal_record: 勋章定义与获得记录分离
```

AI 输出了完整的 CREATE TABLE 语句，包含了索引建议和种子数据。

### 1.3 关键架构决策记录

| 决策 | 理由 | AI 的角色 |
|------|------|-----------|
| 保持 Thymeleaf SSR 不切 SPA | 3 天工期不允许 | AI 评估工期约束后建议 |
| ChildProfile 独立表而非嵌入 User | 支持多孩子家庭 | AI 对比两种设计的优劣后推荐 |
| sport_project(模板) + family_plan(计划) 双表 | 管理员维护模板，用户创建计划 | AI 分析需求后提出的拆分方案 |
| DeepSeek 用 RestTemplate 而非 SDK | 轻量、无额外依赖 | AI 对比后选最简方案 |

---

## 二、AI 编程工作流

### 2.1 核心流程：Plan → Implement → Verify → Iterate

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  PLAN    │ → │ IMPLEMENT│ → │  VERIFY  │ → │ ITERATE  │
│ 探索+设计 │    │ 批量生成  │    │ 编译+测试 │    │ 修bug+优化│
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

**每个阶段的 AI 使用方式：**

### 2.2 Plan 阶段：用 Explore Agent 快速理解代码

```text
提示词模板：
"探索 {目标目录} 的完整结构，列出所有 {类型} 文件的路径和关键内容。
重点关注 {领域} 的实现模式。"

示例：
"探索 src/main/java/com/sportsblog/service/impl/ 目录，
列出所有 ServiceImpl 文件，重点关注文件上传和评论树组装的实现模式。"
```

### 2.3 Implement 阶段：分层次批量生成

**层次 1：Entity（数据模型）**

```text
提示词模板：
"创建 {表名} 的实体类，遵循以下规范：
- 包名：com.sportsblog.entity
- 注解：@Data @TableName @TableId(type=IdType.AUTO) @TableLogic @TableField(fill=...)
- 字段列表：{字段名:类型:注释}
- 参考已有实体：{现有实体路径} 的编码风格"

示例输出：70 行标准 Entity 类，零修改直接可用
```

**层次 2：Mapper + XML**

```text
提示词模板：
"为 {Entity} 创建 Mapper 接口和 XML 映射文件。
BaseMapper<{Entity}> 继承，自定义方法：{方法列表}
XML 中需要 JOIN {关联表} 查询，ResultMap 继承 BaseResultMap 模式。"
```

**层次 3：Service**

```text
提示词模板：
"参考 {现有 ServiceImpl} 的文件上传和事务管理模式，
创建 {新 Service} 的接口和实现。需要注入的 Mapper：{列表}。
关键业务逻辑：{描述}。需要触发后置操作：{描述}。"
```

**层次 4：Controller**

```text
提示词模板：
"创建 {Controller}，遵循 Skill 规范：
- 页面路由：GET /{path}，返回 Thymeleaf 模板名
- API 路由：POST/GET /api/{path}，@ResponseBody + Result<T>
- 需要注入的 Service：{列表}
- 参考现有 Controller：{路径}"
```

**层次 5：前端模板**

```text
提示词模板：
"生成 Thymeleaf 页面 {文件名}.html：
- 继承 fragments/header.html 导航栏
- 使用 Bootstrap 5 栅格 + custom.css 配色变量
- 配色：--joy-primary #FFB74D / --joy-secondary #81C784 / --joy-bg #FFF8E1
- 交互示例：{描述需要的 AJAX 操作和 DOM 更新}
- 参考已有页面：{路径}"
```

### 2.4 Verify 阶段

```text
"mvn compile -q && echo BUILD OK"     ← 每次改动后必跑
"curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/{路径}"  ← 页面可达性
"curl -s http://localhost:8080/api/{端点} | python -c 'print(json.load(...))'"  ← API 数据校验
```

### 2.5 Iterate 阶段

```text
提示词模板：
"当前页面 {页面名} 存在 {具体问题}。
请修改 {文件路径} 的 {具体区域}，参考 {市面主流产品} 的交互模式。"
```

示例：「当前页面打卡表单的星级评分使用了 String.repeat()，但项目是 Java 8 不支持这个方法。请修改为硬编码方案。」

---

## 三、可复用的代码生成 Prompt 模板

### 3.1 Spring Boot Entity 生成

```
按以下规范生成 {EntityName}.java：
- 包：com.sportsblog.entity
- @Data @TableName("{table_name}")
- @TableId(type = IdType.AUTO) 主键 id
- @TableLogic Integer isDeleted
- @TableField(fill = FieldFill.INSERT) LocalDateTime createTime
- @TableField(fill = FieldFill.INSERT_UPDATE) LocalDateTime updateTime
- 字段：{列表}
- 每个字段加 /** 中文注释 */
- 文件路径：src/main/java/com/sportsblog/entity/{EntityName}.java
```

### 3.2 MyBatis-Plus 自定义查询 XML

```
为 {MapperName} 生成 XML 映射文件：
- namespace：com.sportsblog.mapper.{MapperName}
- BaseResultMap 包含所有字段的 column→property 映射
- {查询方法列表，每个指定 resultMap/resultType/参数/WHERE条件}
- 分页查询使用 IPage + Page 参数
- JOIN 查询使用 LEFT JOIN + 别名
```

### 3.3 全栈 CRUD 功能

```
为 {Domain} 创建完整的后端代码：
1. Entity：{表名}，字段 {列表}
2. Mapper：继承 BaseMapper + 自定义查询 {列表}
3. Service 接口 + ServiceImpl：方法 {列表}
4. Controller：页面路由 {列表} + API 路由 {列表}
5. 前端：{页面描述}

遵循 Skill.md 命名规范和 Result<T> 返回值规范。
```

### 3.4 Bug 诊断

```
当前错误：{错误日志}
相关文件：{文件路径列表}
请定位根因并给出修复方案（不改代码，先诊断）。
```

### 3.5 需求对齐检查

```
请对比 {需求文档路径} 和当前代码实现，列出：
1. 已实现的功能
2. 字段级别缺失（具体列出缺失字段名）
3. 设计不一致的地方（如命名、结构）
按优先级排序。
```

---

## 四、知识库结构

### 4.1 项目 Skill 规范（Skill.md）

作为所有 AI 代码生成的约束文件，包含：
- 技术栈版本锁定
- 命名规范（包/类/数据库/API 路径）
- 分层职责（Controller禁止业务逻辑/Service禁止操作Request/Mapper禁止业务逻辑）
- 返回值规范（Result<T> 统一封装）
- 异常处理（BusinessException + GlobalExceptionHandler）
- 前端规范（配色/Template/ECharts/AJAX）

**作用：** 每次代码生成前，AI 先读取 Skill.md 作为约束条件，保证生成的 70+ 文件风格一致。

### 4.2 项目知识沉淀

| 文档 | 内容 | 用途 |
|------|------|------|
| `README.md` | 项目概述、技术栈、截图、快速启动 | 面试展示 |
| `DEPLOY.md` | Railway/Docker/手动部署方案 | 运维交接 |
| `PROMPTS.md` | 本文档，AI 编程方法论 | 团队复用 |
| `init.sql` | 数据库 DDL + 种子数据 | 一键初始化 |
| `demo_data.sql` | 28 条演示数据 | 截图/演示用 |
| `custom.css` | 品牌设计 Token | 前端一致性 |

---

## 五、操作文档

### 5.1 本地开发

```bash
# 启动 MySQL（确保 root/root 可连接）
# 执行初始化
mysql -u root -p < src/main/resources/init.sql
# 启动应用
mvn spring-boot:run
# 访问 http://localhost:8080
```

### 5.2 演示数据

```bash
# 注册两个演示用户
curl -X POST http://localhost:8080/api/auth/register \
  -d "username=mama&password=admin123&nickname=乐乐妈妈&childName=乐乐&childGender=0&childBirth=2020-06-15"
curl -X POST http://localhost:8080/api/auth/register \
  -d "username=papa&password=admin123&nickname=妞妞爸爸&childName=妞妞&childGender=1&childBirth=2022-03-20"
# 插入 28 条运动记录 + 互动数据
mysql -u root -p < demo_data.sql
```

### 5.3 AI 功能配置

编辑 `application.yml`：

```yaml
deepseek:
  api:
    key: sk-your-key    # 不填则 AI 降级到规则引擎
```

### 5.4 扩展指南

| 想做什么 | 改哪里 | 提示词模板 |
|----------|--------|-----------|
| 新增运动项目 | `init.sql` sport_project 表 + 种子数据 | 「给 sport_project 添加 5 个新的亲子运动项目种子数据」 |
| 新增勋章 | `init.sql` medal 表 + `MedalServiceImpl.evaluate()` | 「添加一枚新勋章: 条件=..., 图标=..., 名称=...」 |
| 新增页面 | Controller + .html 模板 | 用「3.3 全栈 CRUD 功能」模板 |
| 接入其他 AI 模型 | `AIClientService.java` | 「将 DeepSeek API 替换为通义千问/文心一言，保持接口签名不变」 |

---

## 六、效率数据

| 指标 | 数值 |
|------|------|
| 总开发时间 | ~3 天 |
| 生成 Java 文件 | 70+ |
| 前端页面 | 11 个 |
| API 端点 | 30+ |
| 数据库表 | 12 张 |
| AI 生成代码一次通过率 | ~85%（Entity/Mapper/Controller 层最高，前端模板需 2-3 轮迭代） |
| 最耗时调试 | Railway 部署（~1h，最终判定为免费版 DNS 限制，止损） |
| 最成功的 AI 辅助环节 | 数据模型设计——从需求文档到完整 DDL 仅 15 分钟 |

---

**版本：** v1.0 | **最后更新：** 2026-06-26
