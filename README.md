# 🥗 饮食记录小程序

记录饮食摄入、运动消耗和营养目标的微信小程序。当前仓库包含可运行原型，后续开发统一以优化版 PRD 和 UI 为准。

## 当前 M0–M1 基线

- Java 17、Spring Boot 3.4，数据库统一使用 MySQL 8
- 环境变量密钥、统一 API 错误、请求 ID、DTO 校验和受限头像上传
- 小程序唯一登录 Promise、401 互斥刷新和开发/体验/生产环境地址
- 五项导航：今日、趋势、记录、运动、我的
- 暖米白/深苔绿设计 Token、公共组件和组件预览页

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

### 后端
先在 MySQL 8 中创建空库：

```sql
CREATE DATABASE diet_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

复制 `.env.example` 中的变量名到本机环境，填入已轮换的真实凭据，然后：

```bash
cd diet-tracker/backend
sh start.sh
```

应用会在 MySQL 8 空库中按版本执行 V1–V5 SQL。已有历史数据的 MySQL 8 库使用仓库提供的安全迁移脚本完成备份、结构升级和数据校验，具体见 [`docs/mysql8-data-migration.md`](docs/mysql8-data-migration.md)。不得直接清库或手工修改数据库迁移记录。

### 小程序
微信开发者工具直接打开 `diet-tracker/miniapp`，不要打开仓库的上层目录。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | 微信小程序 |
| 后端 | Spring Boot 3.4.4；Java 17 |
| 数据库 | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| 构建 | Maven 3.9.9 |

> 文档入口见 [`docs/README.md`](docs/README.md)，开发前请先阅读 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。
