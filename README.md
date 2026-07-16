# 🥗 饮食记录小程序

记录饮食摄入、运动消耗和营养目标的微信小程序。当前统一按优化版 PRD 和 UI 开发，并采用“全部功能先在本地完成，项目完成后再考虑云端部署”的策略。

## 当前 M0–M10 基线

- Java 17、Spring Boot 3.4，数据库统一使用 MySQL 8
- 环境变量密钥、统一 API 错误、请求 ID、DTO 校验和受限头像上传
- 小程序唯一登录 Promise、401 互斥刷新和本地/未来生产环境隔离
- 五项导航：今日、趋势、记录、运动、我的
- 暖米白/深苔绿设计 Token、公共组件和组件预览页
- 用户目标、个人中心、匿名账号删除审计、健康检查与生产限流

## 优化版目标

- 一级导航：今日、趋势、记录、运动、我的
- 核心页面：8 个优化版页面
- UI 基准：[`design/优化版/`](design/优化版/)
- 现在开始：[`docs/START-HERE.md`](docs/START-HERE.md)
- 开发顺序：[`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)
- 阶段文档：[`docs/milestones/`](docs/milestones/README.md)

## 项目结构

```text
diet-tracker/
├── backend/          # Spring Boot 后端
│   ├── src/main/java/com/diettracker/
│   │   ├── controller/    # REST API
│   │   ├── service/       # 业务逻辑
│   │   ├── repository/    # 数据访问
│   │   ├── entity/        # JPA 实体
│   │   ├── security/      # JWT 与鉴权过滤器
│   │   └── config/        # Web 配置
│   ├── src/main/resources/db/migration/ # MySQL 8 迁移
│   └── start.sh           # 测试并启动本地 Profile
├── miniapp/          # 微信小程序前端（开发者工具直接打开此目录）
│   ├── pages/             # 5 个一级页面与饮食日历
│   ├── components/        # M1 公共组件
│   ├── packageTools/      # 组件预览分包
│   ├── services/          # 请求层与业务 API
│   ├── shared/            # 配置、常量、工具和 WXS
│   └── assets/            # 图片与图标
├── docs/             # PRD、接口、架构与上线文档
└── design/           # UI 设计稿和设计说明
```

## 快速启动

### 本地完整环境

```bash
cd /Users/z/Documents/微信小程序/diet-tracker-master
bash backend/scripts/check-e1-environment.sh
bash deploy/local/start.sh
```

该命令在本机 Docker 中启动 MySQL 8.0.46 和 Java 17 后端，执行 Flyway V1–V8，并验证 7 个分类和 48 个系统食品。无需阿里云、域名或 HTTPS。真实微信登录需把 Secret 写入 Git 忽略的本地文件，见 [`docs/LOCAL-DEVELOPMENT.md`](docs/LOCAL-DEVELOPMENT.md)。

### 小程序
微信开发者工具直接打开 `miniapp/`，不要打开仓库上层目录。开发者工具和同一 Wi-Fi 手机使用 `http://192.168.3.25:8080/api`；换网络时按本地手册覆盖地址。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | 微信小程序 |
| 后端 | Spring Boot 3.4.4；Java 17 |
| 数据库 | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| 构建 | Maven 3.9.9 |

> 文档入口见 [`docs/README.md`](docs/README.md)，开发前请先阅读 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。
