# E1 优化版 UI 覆盖审计

审计日期：2026-07-16

设计基准：`design/优化版/` 8 张 390×844 页面稿

## 覆盖矩阵

| 设计图 | 当前页面 | 自动化状态 | 既有视觉证据 | E1 当前证据结论 |
|---|---|---|---|---|
| 01 首页热量总览 | `pages/index` | `dashboard-flow` 通过 | `design/qa/m6-today-dashboard.png` | 当前 GUI 通过：575 摄入、100 运动、1426 剩余、3 餐 |
| 02 热量计算 | `packageFood/.../calorie-calculator` | `calorie-calculator-flow` 通过 | `design/qa/m4-devtools-full.jpeg` | 历史证据存在；真实 MySQL 150g=248kcal 已对账 |
| 03 今日运动与推荐 | `pages/exercise` | `exercise-flow` 通过 | `design/qa/m7-exercise-devtools-full.png` | 历史证据存在；真实 MySQL CRUD 已对账 |
| 04 热量与运动趋势 | `pages/stats` | `trend-flow` 通过 | `design/qa/m8-trends-devtools-full.jpeg` | 当前 GUI 通过：7/30/90 天范围、图表语义和汇总均加载 |
| 05 食品搜索与记录 | `pages/add` | `food-search-flow` 通过 | `design/qa/m3-devtools-full.png` | 历史证据存在；系统/自定义/隔离已对账 |
| 06 饮食日历 | `packageFood/.../calendar` | `calendar-flow` 通过 | `docs/evidence/m9/calendar-comparison.png` | 当前 GUI 通过：2026-07 有记录标记、16 日摘要和三餐明细 |
| 07 餐次详情 | `packageFood/.../meal-detail` | `meal-detail-flow` 通过 | M5 文档记录曾验收，但仓库无独立截图 | 当前 GUI 通过：早餐、鸡胸肉 150g、248 千卡、营养汇总 |
| 08 个人中心 | `pages/profile` | `profile-flow` 通过 | `docs/evidence/m10/profile-devtools.jpeg` | 历史证据存在；目标/资料/删除 API 已对账 |

## 页面与状态

- 五个一级导航和三个核心二级页面均在 `app.json` 注册。
- 首页、趋势、记录、运动、个人中心、热量计算、日历和餐次详情均实现加载、错误、成功状态；业务列表页面另有空状态。
- 饮食和运动写入存在提交中保护；饮食新增有服务端幂等键；编辑/删除有确认和错误反馈。
- 优化版页面索引规定的“首页→记录→计算→餐次详情”和“首页→日历”入口均存在。

## 本轮开发者工具事实

1. 初次读取时工具打开的是旧工作树 `/Users/z/Documents/微信小程序/diet-tracker/miniapp`，其网络错误不可作为 E1 当前证据。
2. 已导入当前 `/Users/z/Documents/微信小程序/diet-tracker-master/miniapp`，并选择“不使用云服务”。
3. 工作区所有者已完成信任确认；普通编译成功，问题面板显示 0。
4. 隔离的合成预览账号完成首页、7/30/90 天趋势、当前月日历和早餐详情走查。它不经过 `wx.login`，不能替代 E1.2 登录验收。
5. 当前仍缺少 iOS/Android 真机视觉证据、日历跨月手势和头像失败态复测。

## 结论

源码、自动化、历史视觉证据和当前模拟器成功态证明 8 张设计均已有对应实现，当前工作树普通编译及核心主链路 GUI 已通过。真实微信登录和 iOS/Android 双端仍是明确缺口；合成预览不得计入真实登录或真机通过率。
