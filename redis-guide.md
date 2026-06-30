# Redis 缓存验证指南

## 启动

| 步骤 | 命令 | 终端 |
|------|------|------|
| 启动 Redis | `cd E:/浏览器下载/Redis-x64-5.0.14.1 && ./redis-server.exe --port 6380` | 终端 2（别关） |
| 连接 Redis | `cd E:/浏览器下载/Redis-x64-5.0.14.1 && ./redis-cli.exe -p 6380` | 终端 3（敲命令用） |
| 启动项目 | `mvn spring-boot:run` | 终端 1 |

## 常用命令速查

```bash
# ---------- 基础 ----------
keys *              # 查看所有缓存 key
FLUSHALL            # 清空所有缓存（重新开始验证时用）
exit                # 退出 redis-cli

# ---------- 单个缓存 ----------
TTL userStats::1            # 查过期倒计时（秒），-1=永不过期，-2=已删除
TYPE userStats::1           # 查数据类型（hash/string/list）
GET userStats::1            # 如果是 string 类型，直接看值

# ---------- 看全部 ----------
keys sportProject::*        # 所有运动项目缓存
keys medal::*               # 所有勋章缓存
keys userStats::*           # 所有用户统计缓存
```

## 三个缓存的验证流程

### 1. 用户公开主页统计 — userStats（10min TTL）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 浏览器访问 `/user/1` | — | 页面显示统计数据 |
| | `keys userStats::*` | `userStats::1` |
| | `TTL userStats::1` | ~600 秒 |
| 10 分钟内再次访问 `/user/1` | — | 页面秒开，SQL 日志里不再有 count/select |
| 用 `mama` 打一次卡 | — | 打卡成功 |
| | `keys userStats::*` | `(empty)` — 被 @CacheEvict 清掉 |
| 再次访问 `/user/1` | — | 重建缓存 |
| | `keys userStats::*` | `userStats::1` 回来了 |

### 2. 运动项目 — sportProject（24h TTL）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 浏览器访问打卡页面 `/checkin` | — | 项目下拉框正常 |
| | `keys sportProject::*` | `sportProject::all` |
| 管理员新增/修改/删除项目 | — | — |
| | `keys sportProject::*` | `(empty)` — allEntries 清空 |
| 再次访问 `/checkin` | — | 重建缓存 |

### 3. 勋章定义 — medal（24h TTL）

| 操作 | redis-cli | 预期 |
|------|-----------|------|
| 浏览器访问勋章馆 `/medals` | — | 勋章列表正常 |
| | `keys medal::*` | `medal::all` |
| 用户达成新勋章 | 打一次卡看日志里的 `[ASYNC] Medal awarded` | — |
| | — | 缓存不受影响（勋章定义不改） |
| 24 小时后 | — | 自动过期，下次访问重建 |

## 排查问题

| 现象 | 可能原因 | 检查 |
|------|---------|------|
| `keys *` 始终空 | 项目没连上 Redis | `application-dev.yml` 的 `spring.redis.port` 是不是 `6380` |
| | 注解没生效 | `@EnableCaching` 在 `CacheConfig` 上吗 |
| | 方法内部调用 | `getAll()` 是否被同类方法直接调（不走代理） |
| 缓存没被清除 | 调的不是代理对象 | 确认 `@CacheEvict` 方法是通过 `@Autowired` 接口调用的 |
| 取出来反序列化报错 | 类型不匹配 | `GenericJackson2JsonRedisSerializer` 会存类型信息 |

## 面试演示脚本（1 分钟）

```bash
# 1. 访问用户主页 → 缓存写入
redis-cli -p 6380
> keys *                          # → userStats::1

# 2. 打卡 → 缓存清除
> keys *                          # → (empty)

# 3. 再访问 → 缓存重建
> keys *                          # → userStats::1

# 4. 运动项目也是同样逻辑
> keys sportProject::*            # → sportProject::all
```
