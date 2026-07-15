# M6：今日热量总览

**状态：** 已完成（待验收合并）
**依赖：** M5
**Git 分支：** `codex/m6-today-dashboard`
**阶段标签：** `m6-complete`

## UI 基准

`01-首页-热量总览.png`

重点元素：剩余热量圆环、今日摄入、目标、运动消耗、三大营养素、记录一餐、今日运动和餐次摘要。

## 目标

用一个聚合接口驱动首页，用户进入后快速知道“还能吃多少、动了多少、下一步做什么”。

## 后端

- 实现 `DashboardService`，不由 Controller 拼接多个 Service 结果。
- 返回目标、饮食摄入、剩余、运动消耗、营养素、运动摘要、餐次摘要和建议。
- 剩余可摄入为 `max(目标 - 摄入, 0)`。
- 运动消耗不自动返还到剩余额度。
- 对同一天数据保持一致性，避免首页多个旧接口结果不同步。

## 小程序

- 实现热量圆环和营养进度条。
- “记录一餐”进入 M3，“相机”进入拍照留存。
- 日历入口进入 M9，未完成前使用明确占位。
- 已成功模块可展示，失败模块独立重试。
- 无记录、超出目标和无运动均有专用状态。

## 接口

- `GET /dashboard/today?date=yyyy-MM-dd`

## 测试与验收

- [x] 首页数据一次请求返回并与餐次详情一致。
- [x] 超出目标时剩余显示 0，并显示超出数值。
- [x] 运动消耗单独展示，不改变剩余额度。
- [x] 部分模块失败不会清空整个首页。
- [x] 首页关键数据接口性能达到 PRD 目标。

验收记录：

- `DashboardApiTest` 覆盖用户隔离、餐次/首页一致、超额归零和默认目标；本地 H2 + MockMvc 三个聚合场景总计 67ms。
- PRD 的部署环境 P95 ≤ 800ms 仍作为上线监控门槛，不用单次本机耗时替代生产采样。
- 视觉验收见 [`design-qa.md`](../../design-qa.md)，实现截图为 [`design/qa/m6-today-dashboard.png`](../../design/qa/m6-today-dashboard.png)。

## Git 交付

```text
feat(m6): add today dashboard aggregation
feat(m6): rebuild optimized today page
test(m6): verify dashboard calculation consistency
```

推送阶段分支，验收合并后创建 `m6-complete` 标签。
