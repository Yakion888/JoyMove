# 🚀 悦动宝 JoyMove 部署指南

## 方案一：Railway（推荐，5 分钟上线）

### 前置条件
- [Railway](https://railway.app) 账号（GitHub 一键登录）
- 安装 [Railway CLI](https://docs.railway.app/develop/cli)：`npm install -g @railway/cli`

### 部署步骤

```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 登录 Railway
railway login

# 3. 初始化项目
railway init

# 4. 添加 MySQL（Railway 自动创建实例）
railway add mysql

# 5. 设置 DeepSeek API Key
railway variables set DEEPSEEK_KEY=sk-your-real-key

# 6. 部署
railway up
```

### 部署后

Railway 会分配一个公网域名（如 `joymove.up.railway.app`）。

**初始化数据库：**
```bash
railway connect mysql
# 进入 MySQL 命令行后执行
source src/main/resources/init.sql;
```

### 演示账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 家长 | mama | admin123 |

---

## 方案二：手动部署（阿里云/腾讯云）

1. 购买云服务器（2核4G，CentOS 7+）
2. 安装 JDK 8 + MySQL 8.0 + Nginx
3. 上传 JAR 包：`scp target/sports-blog-1.0.0-SNAPSHOT.jar root@服务器IP:/app/`
4. 执行 init.sql 初始化数据库
5. 启动：`nohup java -jar /app/sports-blog-1.0.0-SNAPSHOT.jar &`
6. 配置 Nginx 反向代理 + HTTPS

---

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQLHOST` | 数据库主机 | Railway 自动注入 |
| `MYSQLPORT` | 数据库端口 | Railway 自动注入 |
| `MYSQLUSER` | 数据库用户 | Railway 自动注入 |
| `MYSQLPASSWORD` | 数据库密码 | Railway 自动注入 |
| `MYSQLDATABASE` | 数据库名 | Railway 自动注入 |
| `DEEPSEEK_KEY` | DeepSeek API Key | `sk-demo`（不设则 AI 降级到规则引擎） |
