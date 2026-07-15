# M9：饮食日历

**状态：** 已完成
**依赖：** M8
**Git 分支：** `codex/m9-calendar`
**阶段标签：** `m9-complete`

## UI 基准

`06-饮食日历.png`

重点元素：月切换、有记录标记、选中日期、当日摄入、目标进度、餐次与运动摘要。

## 目标

用一次月度摘要请求驱动日历，替代旧版手输日期和逐日请求。

## 后端

- 实现月度摘要接口，一次返回整月每日状态。
- 每日包含摄入、运动、剩余、餐次数量和是否有记录。
- 月份参数严格校验，默认限制最近 12 个月。
- 查询基于用户与日期索引，避免逐日 N+1 查询。

## 小程序

- 实现真实月历、跨月日期、今天和选中状态。
- 有记录日期显示轻量标记。
- 切换月份只发一次摘要请求。
- 选中日期后展示热量进度、餐次数和运动消耗。
- 查看详情进入当天餐次内容。

## 接口

- `GET /calendar/summary?month=yyyy-MM`
- 复用 `GET /dashboard/today?date=` 或餐次查询加载选中日详情。

## 测试与验收

- [x] 大小月、闰年、跨年和周起始显示正确。
- [x] 月切换不会逐日发起接口请求。
- [x] 日期标记与真实记录一致。
- [x] 选中日期摘要与首页相同日期结果一致。
- [x] 网络失败保留当前日历并提供重试。

自动化覆盖：`CalendarApiTest`、`calendar-date.test.js`、`calendar-flow.test.js`。整月摘要对饮食和运动各执行一次分组查询，并与同日 `dashboard/today` 完成对账。微信开发者工具 Nightly 2.02.2607142 普通编译通过，问题面板 0 项；成功态视觉证据见 [`../evidence/m9/calendar-comparison.png`](../evidence/m9/calendar-comparison.png) 和项目根目录 `design-qa.md`。

## Git 交付

```text
feat(m9): add monthly calendar summary API
feat(m9): implement optimized diet calendar
test(m9): cover calendar boundaries and query count
```

PR [#11](https://github.com/Lanlnn/diet-tracker/pull/11) 已通过完整 CI 并 squash 合并到 `master`；完成标签 `m9-complete` 已创建并推送。
