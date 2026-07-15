# M8：热量与运动趋势

**状态：** 已完成
**依赖：** M7
**Git 分支：** `codex/m8-trends`
**阶段标签：** `m8-complete`

## UI 基准

`04-热量与运动趋势.png`

重点元素：7/30/90 天切换、日均净摄入、柱状趋势、平均摄入、平均运动和规则化总结。

## 目标

在同一统计语境中展示饮食摄入、运动消耗和净摄入，不沿用旧版仅统计饮食热量的页面。

## 后端

- 实现统一趋势查询服务，支持 7d/30d/90d。
- 返回完整日期序列，缺失日期补 0。
- 计算平均摄入、平均运动、日均净摄入和营养达成率。
- 数据不足三天时不生成趋势结论。
- 查询基于快照字段并增加必要索引。

## 小程序

- 实现 7/30/90 天分段选择。
- 图表颜色、选中态和文字层级遵循设计 Token。
- 图表具备无障碍文字摘要，不只依靠颜色。
- 展示最多两条规则化周总结。
- 请求切换时取消或忽略过期响应。

## 接口

- `GET /stats/trend?range=7d|30d|90d`

## 测试与验收

- [x] 净摄入等于饮食摄入减运动消耗。
- [x] 日期序列连续且时区边界正确。
- [x] 少于三天数据时只提示继续记录。
- [x] 切换范围不会显示上一请求的过期结果。
- [x] 统计结果与相同日期的首页、记录数据对账一致。

自动化覆盖：`TrendApiTest`、`trend-flow.test.js`。查询复用 `idx_meal_record_user_date` 与 `idx_exercise_record_user_date`，避免新增重复索引。后端 32 项测试通过（本地跳过 1 项外部 MySQL 用例，由 CI 运行），小程序 9/9 测试通过。微信开发者工具 Nightly 2.02.2607142 普通编译通过（问题面板 0 项）；通过与生产响应 DTO 一致的本地契约服务核对了 7/30/90 天成功态、少于 3 天状态、加载失败和重试状态，截图见 `design/qa/m8-trends-devtools-full.jpeg`。

## Git 交付

```text
feat(m8): add unified intake and exercise trends
feat(m8): implement optimized trends page
test(m8): verify date ranges and aggregate accuracy
```

阶段 PR 经 CI 验收合并后，从合并后的 `master` 创建 `m8-complete` 标签。

## 闭环结果

M8 按 M7 → M8 的依赖顺序交付。完整顺序和防复发规则见 [`DEVELOPMENT-RETROSPECTIVE.md`](../DEVELOPMENT-RETROSPECTIVE.md#3-m8-闭环退出条件)。

- [x] M7 合并并创建 `m7-complete` 标签。
- [x] M8 整理到包含 M7 的最新 `master`，PR 中只保留 M8 范围。
- [x] 使用契约兼容的本地服务完成成功态、少于 3 天态、30/90 天图表视觉验收并保存截图。
- [x] M8 PR 的 CI 全绿，合并后创建 `m8-complete` 标签。
