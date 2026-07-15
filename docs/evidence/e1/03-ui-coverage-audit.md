# E1 优化版 UI 覆盖审计

审计日期：2026-07-16

设计基准：`design/优化版/` 8 张 390×844 页面稿

## 覆盖矩阵

| 设计图 | 当前页面 | 自动化状态 | 既有视觉证据 | E1 当前证据结论 |
|---|---|---|---|---|
| 01 首页热量总览 | `pages/index` | `dashboard-flow` 通过 | `design/qa/m6-today-dashboard.png` | 历史证据存在；当前工作树 GUI 待信任确认 |
| 02 热量计算 | `packageFood/.../calorie-calculator` | `calorie-calculator-flow` 通过 | `design/qa/m4-devtools-full.jpeg` | 历史证据存在；真实 MySQL 150g=248kcal 已对账 |
| 03 今日运动与推荐 | `pages/exercise` | `exercise-flow` 通过 | `design/qa/m7-exercise-devtools-full.png` | 历史证据存在；真实 MySQL CRUD 已对账 |
| 04 热量与运动趋势 | `pages/stats` | `trend-flow` 通过 | `design/qa/m8-trends-devtools-full.jpeg` | 历史证据存在；7d API 已对账，30/90d 待当前 GUI |
| 05 食品搜索与记录 | `pages/add` | `food-search-flow` 通过 | `design/qa/m3-devtools-full.png` | 历史证据存在；系统/自定义/隔离已对账 |
| 06 饮食日历 | `packageFood/.../calendar` | `calendar-flow` 通过 | `docs/evidence/m9/calendar-comparison.png` | 历史证据存在；当前月 API 已对账，跨月待当前 GUI |
| 07 餐次详情 | `packageFood/.../meal-detail` | `meal-detail-flow` 通过 | M5 文档记录曾验收，但仓库无独立截图 | **证据缺口：需在当前工作树补截图** |
| 08 个人中心 | `pages/profile` | `profile-flow` 通过 | `docs/evidence/m10/profile-devtools.jpeg` | 历史证据存在；目标/资料/删除 API 已对账 |

## 页面与状态

- 五个一级导航和三个核心二级页面均在 `app.json` 注册。
- 首页、趋势、记录、运动、个人中心、热量计算、日历和餐次详情均实现加载、错误、成功状态；业务列表页面另有空状态。
- 饮食和运动写入存在提交中保护；饮食新增有服务端幂等键；编辑/删除有确认和错误反馈。
- 优化版页面索引规定的“首页→记录→计算→餐次详情”和“首页→日历”入口均存在。

## 本轮开发者工具事实

1. 初次读取时工具打开的是旧工作树 `/Users/z/Documents/微信小程序/diet-tracker/miniapp`，其网络错误不可作为 E1 当前证据。
2. 已导入当前 `/Users/z/Documents/微信小程序/diet-tracker-master/miniapp`，并选择“不使用云服务”。
3. 当前停在开发者工具的“信任并运行”安全确认，必须由工作区所有者亲自确认。
4. 确认后必须补做普通编译问题面板 0、07 餐次详情截图、7/30/90 趋势、日历跨月和头像失败态。

## 结论

源码、自动化和历史视觉证据证明 8 张设计均已有对应实现，但不能证明当前 E1 提交在开发者工具和实体设备上全部通过。餐次详情截图以及当前分支 GUI/双端证据仍是明确缺口。
