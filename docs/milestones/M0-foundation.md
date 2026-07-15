# M0：工程、安全与数据基线

**状态：** 已完成
**Git 分支：** `codex/m0-foundation`
**阶段标签：** `m0-complete`

## 目标

把当前原型整理成安全、可重复构建、可测试、可迁移的开发基线。M0 不开发新页面。

## 范围

### 仓库与配置

- 清理旧工具配置、重复目录和未归档的结构改动。
- 提交当前目录重构，建立可回退基线。
- 增加 `.editorconfig`、环境变量示例和本地启动说明。
- 统一 Java 开发、编译和部署版本。
- MySQL 8 作为唯一数据库，删除其他数据库驱动和专用 SQL。

### 安全

- 数据库密码、微信 Secret、JWT Secret 只从环境变量读取。
- 轮换已经进入 Git 历史的真实凭据。
- 删除 Token、微信登录响应和完整 POST 请求体日志。
- Seed/诊断接口仅允许本地 Profile，生产环境不可访问。
- 限制上传类型、大小和公开访问路径。

### 后端基础

- 引入 Flyway 并建立基线迁移。
- 引入 Request/Response DTO、Jakarta Validation 和统一异常响应。
- 错误格式固定为 `code/message/requestId/fieldErrors`。
- 建立 `local/test/prod` 配置。
- 为 `user_id + date` 等高频查询建立索引。

### 小程序基础

- 建立唯一登录 Promise 和 401 互斥刷新。
- 页面请求等待登录完成。
- 建立开发、测试、生产 API 地址切换。
- 删除敏感控制台日志。
- 区分 loading、empty 和 error，禁止把错误吞成空数组。

## 测试

- 后端 `clean test` 可在全新环境运行。
- 配置缺少必要环境变量时启动应明确失败。
- 首次登录只发起一次微信登录请求。
- 多接口同时 401 时只发生一次重新登录。
- 仓库密钥扫描无真实凭据。

## 验收

- [x] 新开发者只看文档即可启动项目。
- [x] MySQL 8 迁移可从空库完整执行。
- [x] CI 启动真实 `mysql:8.0` 服务验证 Flyway 和 Hibernate 表结构。
- [x] CI 自动执行构建、测试和基础扫描。
- [x] 当前核心饮食流程仍可运行。
- [x] 文档和代码不再声明互相冲突的数据库或 Java 版本。

## Git 交付

建议提交：

```text
chore(m0): establish secure development baseline
test(m0): add build and authentication checks
docs(m0): document environments and migrations
```

推送阶段分支，验收合并后创建 `m0-complete` 标签。
