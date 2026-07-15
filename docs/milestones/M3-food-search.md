# M3：食品库与搜索记录入口

**状态：** 已完成
**依赖：** M2
**Git 分支：** `codex/m3-food-search`
**阶段标签：** `m3-complete`

## UI 基准

`05-食品搜索与记录.png`

重点元素：顶部搜索框、拍照留存、自定义食物、常用/最近/收藏、食品列表、最近组合。

## 目标

完成食品发现入口，并把食品营养数据统一到每 100g 基准，为 M4 热量计算提供可靠输入。

## 数据与后端

- `FoodItem` 增加 `baseAmount/baseUnit/servingAmount/servingUnit/source`。
- 明确系统食品和用户自定义食品归属。
- 迁移并校验现有按份数据，禁止与每 100g 数据混算。
- 搜索支持分页、关键词清洗和空关键词保护。
- 提供常用、最近、收藏查询和收藏开关。
- 用户不能读取其他用户的自定义食品。

## 小程序

- 搜索输入防抖 300ms，旧请求结果不能覆盖新关键词。
- 常用、最近、收藏切换保持独立状态。
- 食品行展示名称、每 100g 热量、图片/占位图和添加按钮。
- 无结果时提供创建自定义食品入口。
- 拍照按钮本阶段只进入留存流程占位，不做 AI 识别。
- 点击食品进入 M4 热量计算页。

## 接口

- `GET /foods?scope=common|recent|favorite&page=&size=`
- `GET /foods/search?keyword=&page=&size=`
- `POST /foods`
- `PUT /foods/{id}/favorite`

## 测试与验收

- [x] 搜索“鸡”返回匹配食品；查询支持分页和 50 条上限，接口 P95 ≤ 500ms 作为部署监控门槛。
- [x] 快速连续输入不会出现结果倒序覆盖。
- [x] 自定义食品只对创建者可见。
- [x] 每个食品明确显示营养基准单位。
- [x] loading、empty、error 和 retry 状态完整。
- [x] 搜索无结果可进入自定义食品流程。

视觉验收记录见 [`design-qa.md`](../../design-qa.md)。接口 P95 需要在带真实数据量的部署环境持续采样，本地集成测试验证查询结果、隔离和分页契约，不以单次本机耗时代替生产 P95。

## Git 交付

```text
feat(m3): migrate food nutrition baseline
feat(m3): implement food search and recent lists
test(m3): cover food visibility and search states
```

推送阶段分支，验收合并后创建 `m3-complete` 标签。
