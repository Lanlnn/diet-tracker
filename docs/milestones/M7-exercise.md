# M7：今日运动与推荐

**状态：** 未开始
**依赖：** M6
**Git 分支：** `codex/m7-exercise`
**阶段标签：** `m7-complete`

## UI 基准

`03-今日运动与推荐.png`

重点元素：今日消耗、运动时长、已完成运动、添加运动、最多两个推荐和本周完成度。

## 目标

完成运动记录闭环，并把运动消耗接入首页和后续趋势统计。

## 数据与后端

- 新增 `ExerciseRecord`：日期、类型、开始时间、时长、强度、消耗、来源、备注。
- 迁移脚本增加用户与日期索引。
- 实现运动新增、查询、编辑和删除。
- 消耗计算使用配置系数，保存用户最终确认值。
- 推荐由规则引擎生成，V1 不调用大模型自由生成。

## 小程序

- 展示今日总消耗、总时长和运动列表。
- 实现添加、编辑和删除运动流程。
- 推荐最多两项，并展示强度、时长和预计消耗。
- 无记录时提供“添加第一次运动”和低强度推荐。
- 首页运动摘要在保存后同步刷新。

## 接口

- `GET /exercises?date=`
- `POST /exercises`
- `PUT /exercises/{id}`
- `DELETE /exercises/{id}`
- `GET /exercise-recommendations?date=`

## 测试与验收

- [ ] 添加 20 分钟运动后列表和首页同步更新。
- [ ] 越权编辑和删除被拒绝。
- [ ] 推荐不超过两项且不输出医学承诺。
- [ ] 运动消耗不增加首页剩余可摄入。
- [ ] 空、错、加载和删除确认状态齐全。

## Git 交付

```text
feat(m7): add exercise record domain and APIs
feat(m7): implement optimized exercise page
test(m7): cover exercise calculation and isolation
```

推送阶段分支，验收合并后创建 `m7-complete` 标签。
