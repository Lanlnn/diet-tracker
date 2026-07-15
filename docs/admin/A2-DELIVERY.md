# A2 系统食品后台 MVP 交付记录

**状态：** 待验收

**交付分支：** `codex/a2-admin-foods`

**基线：** `master` / `2433800`（M8 已合并）
**记录日期：** 2026-07-15

## 交付范围

- A0：前后台映射、OpenAPI、管理员安全审计 ADR、事件口径冻结。
- A1：独立管理员账号与 JWT、会话失效、RBAC、审计、登录限流和 React 管理端骨架。
- A2：系统食品与分类维护、150g 营养预览、用户私有食品只读诊断、历史快照保护。

M9 日历 Controller、Service、DTO 和仓储聚合不属于本分支。

## 自动化与构建证据

- `backend/./mvnw --batch-mode -q test`：通过，共 36 项测试。
- 前后台贯通契约：后台修改系统食品后，小程序搜索与计算接口读取新值，历史膳食快照保持原值。
- `admin-web/npm ci`：通过，依赖审计 0 漏洞。
- `admin-web/npm run lint`：通过。
- `admin-web/npm run build`：通过。
- `git diff --check`：通过。

## MySQL 8 迁移证据

- 环境：本机 MySQL 8.4.10，临时空库。
- Flyway：V1–V8 共 8 个迁移全部成功，最终版本为 V8。
- Hibernate：`ddl-auto=validate` 通过，应用完成启动。
- 安全冒烟：未携带管理员令牌访问 `/api/admin/auth/me` 返回 `401 ADMIN_AUTH_REQUIRED`。
- 临时验证数据库已在测试后删除。

实机验证曾发现审计摘要 `TEXT` 与 `@Lob` 类型推断不一致；实体已改为显式 `TEXT` 映射，并完成上述回归。

## 浏览器联调证据

- Browser：应用内 Browser，真实 Vite 前端 + Spring Boot + MySQL 8 API。
- 页面身份、非空页面、无框架错误覆盖层、控制台 error/warn：通过。
- 登录 → 阶段总览 → 系统食品列表 → 退出登录：通过。
- 登录表单键盘填写、系统食品真实数据表格、退出后会话回到登录页：通过。
- 桌面视觉与响应式行为已按现有 A2 设计基准检查。

## 合并门禁

- [ ] 推送分支并创建 Draft PR。
- [ ] 远端 CI 全绿。
- [ ] 代码评审通过。
- [ ] 合并至最新 `master` 后记录阶段完成状态。

在这些门禁完成前，A2 保持“待验收”，不提前标记“已完成”。
