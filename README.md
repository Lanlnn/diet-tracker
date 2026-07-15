# 🥗 饮食记录小程序

记录饮食摄入、运动消耗和营养目标的微信小程序。当前仓库包含可运行原型，后续开发统一以优化版 PRD 和 UI 为准。

## 当前原型功能

- **首页**：今日饮食概览，按餐次分组展示，营养汇总
- **记录**：从预置食物列表选择，或自定义输入食物名称和营养数据
- **历史**：按日浏览饮食记录，支持删除
- **统计**：周热量柱状图、日均营养分析、健康建议

## 优化版目标

- 一级导航：今日、趋势、记录、运动、我的
- 核心页面：8 个优化版页面
- UI 基准：[`design/优化版/`](design/优化版/)
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
│   ├── src/main/resources/application.properties
│   └── start.cmd          # 启动脚本
├── miniapp/          # 微信小程序前端（开发者工具直接打开此目录）
│   ├── pages/             # 4 个页面
│   │   ├── index/         # 首页
│   │   ├── add/           # 记录
│   │   ├── history/       # 历史
│   │   └── stats/         # 统计
│   ├── services/          # 请求层与业务 API
│   ├── shared/            # 配置、常量、工具和 WXS
│   └── assets/            # 图片与图标
├── docs/             # PRD、接口、架构与上线文档
└── design/           # UI 设计稿和设计说明
```

## 快速启动

### 后端
```bash
cd diet-tracker/backend
./start.sh
```

### 小程序
微信开发者工具直接打开 `diet-tracker/miniapp`，不要打开仓库的上层目录。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | 微信小程序 |
| 后端 | Spring Boot 3.4.4；Java 版本当前待统一 |
| 数据库 | PostgreSQL（目标技术栈） |
| ORM | Spring Data JPA + Hibernate |
| 构建 | Maven 3.9.9 |

> 文档入口见 [`docs/README.md`](docs/README.md)，开发前请先阅读 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。
