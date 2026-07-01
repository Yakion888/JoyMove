# Redis 缓存验证指南

## 启动

| 步骤 | 命令 | 终端 |
|------|------|------|
| 启动 Redis | `cd E:/浏览器下载/Redis-x64-5.0.14.1 && ./redis-server.exe --port 6380` | 独立 cmd（别关） |
| 连接 Redis | `cd E:/浏览器下载/Redis-x64-5.0.14.1 && ./redis-cli.exe -p 6380` | VSCode 终端（敲命令用） |
| 启动项目 | `mvn spring-boot:run` | VSCode 终端 1 |

## 缓存清单

| 缓存空间 | TTL | 淘汰方式 | 防雪崩 | 存储内容 |
|---------|-----|---------|--------|---------|
| `sportProject` | 永不过期 | @CacheEvict 管理员增删改时清 | —（永不过期无需防） | 运动项目列表、详情 |
| `medal` | 永不过期 | 无主动淘汰 | — | 勋章定义列表 |
| `userStats` | 10min ±15% | TTL 过期 + 打卡 @CacheEvict 精准清 | TTL 随机抖动 | 用户公开主页统计 |
| `nullMarker` | 1min ±15% | TTL 过期 | TTL 随机抖动 | 不存在用户的空标记 |
| `aiReport` | 永不过期 | 无 | — | AI 月报（childId:year:month） |

## 防护机制

| 机制 | 实现 | 位置 |
|------|------|------|
| 缓存穿透 | `nullMarker` 缓存 1min | PublicProfileServiceImpl.getNullMarker() |
| 缓存击穿 | `sync = true` 锁重建 | PublicProfileServiceImpl.getStats() |
| 缓存雪崩 | TTL ±15% 随机抖动 | CacheConfig.redisCacheConfig() |
| 熔断降级 | Redis 不可用 → CacheErrorHandler 回源 DB | CacheConfig.cacheErrorHandler() |
| 缓存预热 | 启动时 CommandLineRunner 主动加载 | CacheWarmUp.java |

## 常用命令速查

```bash
# ---------- 基础 ----------
keys *              # 查看所有缓存 key
FLUSHALL            # 清空所有缓存（重新开始验证时用）
exit                # 退出 redis-cli

# ---------- 单个缓存 ----------
TTL userStats::1            # 查过期倒计时（秒），-1=永不过期，-2=已删除
TYPE userStats::1           # 查数据类型
GET userStats::1            # 如果是 string 类型，直接看值

# ---------- 看全部 ----------
keys sportProject::*        # 运动项目缓存
keys medal::*               # 勋章缓存
keys userStats::*           # 用户统计缓存
keys aiReport::*            # AI 月报缓存
keys nullMarker::*          # 穿透空标记
```

## 各缓存验证流程

### 1. 用户公开主页统计 — userStats（10min）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 浏览器访问 `/user/1` | — | 页面显示统计数据 |
| | `keys userStats::*` | `userStats::1` |
| | `TTL userStats::1` | ~600 秒 |
| 再访问 `/user/1` | — | 缓存命中，59ms → 16ms |
| 用 `mama` 打一次卡 | — | 打卡成功 |
| | `keys userStats::*` | `(empty)` — @CacheEvict 清掉 |
| 再次访问 `/user/1` | — | 重建缓存 |

### 2. 运动项目 — sportProject（永不过期）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 启动项目（预热自动加载） | `keys sportProject::*` | `sportProject::all` + `sportProject::enabled` |
| 管理员新增/修改/删除项目 | — | — |
| | `keys sportProject::*` | `(empty)` — @CacheEvict allEntries 清空 |
| 再次访问 `/checkin` | — | 重建缓存 |

### 3. 勋章定义 — medal（永不过期）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 启动项目（预热自动加载） | `keys medal::*` | `medal::all` |
| 访问 `/medals` | — | 勋章列表，137ms → 38ms |
| 用户达成新勋章 | 注意：勋章定义不改 | 缓存不受影响 |

### 4. AI 月报 — aiReport（永不过期）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 首次点击"生成报告" | `keys aiReport::*` | `aiReport::1:2026:6`（等 DeepSeek 5-10s） |
| 同月再次点击 | — | 瞬间返回，缓存命中 |
| 切换不同月份 | — | 新 key，首次慢，再次秒开 |

### 5. 穿透防护 — nullMarker（1min）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 浏览器访问 `/user/99999` | — | "用户不存在" |
| | `keys nullMarker::*` | `nullMarker::99999` |
| 1 分钟内再次访问 `/user/99999` | — | 直接返回"用户不存在"，不查 DB |
| 1 分钟后 | `keys nullMarker::*` | TTL 过期，自动消失 |

## 实测性能数据

| 接口 | 无缓存 | 缓存命中 | 提升 |
|------|--------|---------|------|
| `/api/user/9/stats` | 33ms | 13ms | **2.5 倍** |
| `/api/medals/progress` | 40ms | 26ms | **1.5 倍** |
| `/api/projects` | 22ms | 13ms | **1.6 倍** |
| AI 月报生成 | 5-10s | 毫秒级 | **100+ 倍** |

## 多级缓存调研

评估了 Caffeine L1 + Redis L2 方案。实测 Spring 的 `CompositeCacheManager` 不执行真正的 L1→L2 级联——其 `getCache(name)` 只返回第一个匹配的缓存管理器，所有读写只走 L1，L2 永不被写入。真正多级缓存需自定义 `Cache` 实现或使用阿里的 JetCache。结论：当前规模下 Redis 单级足够，面试可说明评估过程。

## 排查问题

| 现象 | 可能原因 | 检查 |
|------|---------|------|
| `keys *` 始终空 | Redis 没连上 | 独立 cmd 里 redis-server 在跑吗 |
| | 端口不对 | `application-dev.yml` 的 port 是 `6380` |
| | @Primary 放错位置 | CacheConfig 的 @Primary 在 cacheManager() 上 |
| 访问页面 500 | 关 Redis 后未重启项目 | 重启项目以触发 CacheErrorHandler 兜底 |
| | LocalDateTime 序列化报错 | 确认 JavaTimeModule 已注册 |
| 缓存没被清除 | 同类方法内部调用 | `@CacheEvict` 方法需通过 @Autowired 接口代理调用 |
