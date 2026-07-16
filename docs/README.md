# 项目文档索引

当前统一策略是：先在本地完成全部功能、UI 和双端联调，项目完成后再决定云端部署。任何旧文档中的 staging、阿里云、域名或 HTTPS 描述都不是当前开发前置条件。

## 开发阅读顺序

1. [从这里开始](./START-HERE.md)：当前状态和每次开发循环。
2. [本地开发与联调](./LOCAL-DEVELOPMENT.md)：一键启动、真实登录、局域网真机和排障。
3. [E1 本地功能闭环](./NEXT-DEVELOPMENT.md)：E1.0–E1.4 的执行顺序和完成定义。
4. [开发执行指南](./DEVELOPMENT.md)：产品、工程、接口和测试规范。
5. [PRD](./饮食与运动小程序-产品需求文档-PRD-v1.0.md)：产品范围与验收标准。
6. [UI 基准](../design/优化版/设计说明.md)：页面视觉和交互基准。
7. [系统架构](./architecture.md) 与 [API](./api.md)：当前实现契约。
8. [M0–M10 整体验收](./M0-M10-REVIEW-AND-TEST.md) 与 [里程碑](./milestones/README.md)：历史成果和回归范围。
9. [本地回退与未来发布](./release-and-rollback.md)：本地数据保护及项目完成后的发布门禁。
10. [Git 工作流](./git-workflow.md) 与 [开发复盘](./DEVELOPMENT-RETROSPECTIVE.md)：协作和防复发规则。

## 状态文档

- [E1.0 版本与本地环境冻结](./evidence/e1/00-version-and-environment-freeze.md)
- [E1.1 本地运行记录](./evidence/e1/01-local-runtime.md)
- [E1.2–E1.4 真机验收表](./evidence/e1/02-real-device-acceptance.md)
- [E1 优化版 UI 覆盖审计](./evidence/e1/03-ui-coverage-audit.md)

## 文档维护规则

- 产品规则变更同步更新 PRD、API 和对应里程碑。
- 环境、端口、版本或启动方式变更同步更新 `LOCAL-DEVELOPMENT.md`、架构和根 README。
- 验收结论只写真实执行结果；未执行标记为“待执行”，不以模拟器截图代替真机证据。
- Secret、Token、openid、微信 code 和真实用户数据永不进入文档或 Git。
- `docs/evidence/review-20260715/` 是历史快照，不回写成当前状态。
