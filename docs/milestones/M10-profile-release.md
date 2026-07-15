# M10：个人中心、整体验收与发布准备

**状态：** 待验收
**依赖：** M9
**Git 分支：** `codex/m10-profile-release`
**阶段标签：** `m10-complete`

## UI 基准

`08-个人中心.png`

重点元素：用户资料、当前与目标体重、目标进度、身体与营养目标、食品库、训练计划、AI 教练、服务和设置。

## 目标

完成个人中心和用户目标，并进行 M0–M10 的跨模块验收、隐私和发布准备。

## 数据与后端

- 新增 `UserGoal` 和迁移脚本。
- 实现目标查询与修改；数值范围遵循 PRD。
- 提供自定义食品、收藏、训练计划等入口所需摘要。
- 实现账户数据导出/删除或明确的删除流程。
- 完成上传文件生命周期、日志脱敏、限流和健康检查。
- 完成迁移回滚和生产配置检查。

## 小程序

- 实现资料、体重目标进度和分组菜单。
- 身体与营养目标支持读取和修改。
- 食品库、训练计划、服务与设置按设计收纳。
- AI 教练只作为规则开关或场景入口，不新增独立 Tab。
- 完成隐私说明、账号数据删除和关于页面。

## 接口

- `GET /users/me/goals`
- `PUT /users/me/goals`
- `DELETE /users/me`
- 必要的个人中心摘要接口

## 整体验收

完整演示顺序、跨模块对账、异常矩阵、证据目录和发布候选退出条件见 [`M0–M10 成果复盘与整体验收手册`](../M0-M10-REVIEW-AND-TEST.md)。本阶段不得用自动化通过代替真实 MySQL、开发者工具、真机和发布演练。

- [x] 五项一级导航和 8 张优化版页面全部实现。
- [x] 饮食、运动、首页、趋势和日历数据一致。
- [x] P0 主链路冒烟测试全部通过。
- [x] 计算规则自动化测试达到 PRD 门槛。
- [ ] iOS、Android 各至少两种常见屏幕完成真机验证。
- [x] 仓库无真实密钥，生产无调试/Seed 接口。
- [x] 数据库升级与回滚脚本及 CI 演练通过。
- [x] API 性能、日志、监控和告警达到上线要求。

本地与开发者工具验收：Java 17 + MySQL 8.0.46 下后端完整测试 38/38、小程序 12/12、JavaScript/JSON 静态检查和发布门禁脚本通过；V8 升级、U8 回滚至 V7 和再次升级均成功。关键 API P95 基线为首页 10ms、搜索 8ms、月历 18ms、90 天趋势 18ms；Prometheus 6 条告警规则通过 `promtool`。微信开发者工具 Nightly 2.02.2607142 当前工作树普通编译问题面板为 0。个人中心与设置页证据见 [`../evidence/m10/profile-devtools.jpeg`](../evidence/m10/profile-devtools.jpeg)、[`../evidence/m10/settings-devtools.jpeg`](../evidence/m10/settings-devtools.jpeg) 和项目根目录 `design-qa.md`；完整环境、回滚和收口记录见 [`../evidence/review-20260715/`](../evidence/review-20260715/)。

发布及回滚流程见 [`../release-and-rollback.md`](../release-and-rollback.md)。在真机矩阵和生产灰度监控演练完成前，不创建 `m10-complete` 标签。

## Git 交付

```text
feat(m10): complete profile goals and settings
test(m10): add end-to-end release regression
chore(m10): finalize production readiness
docs(m10): publish release and rollback guide
```

推送阶段分支，验收合并后创建 `m10-complete` 标签。`m10-complete` 只代表达到发布候选标准，正式发布仍需要灰度验证。
