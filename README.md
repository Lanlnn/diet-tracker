# 🥗 饮食记录小程序

记录每日饮食摄入、追踪营养数据的微信小程序。

## 功能

- **首页**：今日饮食概览，按餐次分组展示，营养汇总
- **记录**：从预置食物列表选择，或自定义输入食物名称和营养数据
- **历史**：按日浏览饮食记录，支持删除
- **统计**：周热量柱状图、日均营养分析、健康建议

## 项目结构

```
H:\diet-tracker\
├── backend/          # Spring Boot 后端 (Java 25)
│   ├── src/main/java/com/diettracker/
│   │   ├── controller/    # REST API
│   │   ├── service/       # 业务逻辑
│   │   ├── repository/    # 数据访问
│   │   └── model/         # 实体类
│   ├── src/main/resources/application.properties
│   └── start.cmd          # 启动脚本
├── miniapp/          # 微信小程序前端
│   ├── pages/             # 4 个页面
│   │   ├── index/         # 首页
│   │   ├── add/           # 记录
│   │   ├── history/       # 历史
│   │   └── stats/         # 统计
│   └── utils/             # API 封装、工具函数
└── docs/
    ├── api.md             # API 接口文档
    └── architecture.md    # 系统架构设计
```

## 快速启动

### 后端
```bash
cd H:\diet-tracker\backend
start.cmd
```

### 小程序
微信开发者工具打开 `H:\diet-tracker\miniapp`，AppID 使用测试号。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | 微信小程序 |
| 后端 | Spring Boot 3.4.4 + JDK 25 |
| 数据库 | MySQL 8.0 |
| ORM | Spring Data JPA + Hibernate |
| 构建 | Maven 3.9.9 |

> 详细文档见 [`docs/api.md`](docs/api.md) 和 [`docs/architecture.md`](docs/architecture.md)
