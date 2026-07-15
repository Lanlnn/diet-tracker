# 管理后台 PRD 指标事件口径（A0 冻结）

本文件只冻结事件与指标定义，不授权 A0–A2 制造事件或展示假数据看板。

## 公共事件字段

| 字段 | 类型 | 规则 |
| --- | --- | --- |
| `eventId` | string | 客户端生成 UUID，用于幂等 |
| `eventName` | string | 取下表固定值 |
| `eventVersion` | integer | 首版为 1 |
| `occurredAt` | ISO-8601 | 客户端发生时间，含时区 |
| `anonymousUserId` | string | 服务端不可逆摘要，不使用 openid |
| `sessionId` | string | 匿名会话标识 |
| `pageId` | string | 使用 ADMIN-DEVELOPMENT 第 2 节前台 ID |
| `entrySource` | string | 固定枚举，禁止自由文本 |
| `result` | string | `SUCCESS`、`EMPTY`、`FAILURE` 或 `CANCELLED` |
| `requestId` | string | 有服务端请求时必填 |

禁止作为通用事件维度采集搜索全文、备注全文、昵称、体重、图片地址或 openid。

## 事件字典

| 事件 | 触发时点 | 必需扩展字段 |
| --- | --- | --- |
| `record_entry_click` | 用户从入口进入记录链路 | `entrySource` |
| `meal_record_success` | 服务端成功持久化饮食记录 | `requestId`, `mealType` |
| `food_search` | 有效关键词搜索得到响应 | `result`, `resultCount`, `requestId` |
| `food_search_empty` | 有效搜索结果为 0 | `requestId`, `keywordDigest`（不可逆、不可枚举还原） |
| `dashboard_request` | 首页聚合请求结束 | `result`, `latencyMs`, `requestId` |

## 五项指标

| 指标 | 计算 | 数据源 | 窗口/排除 |
| --- | --- | --- | --- |
| 首次记录完成率 | 首次进入记录链路后 24h 内首次成功保存用户数 / 首次进入用户数 | `record_entry_click`, `meal_record_success` | 按首次进入日归因；测试账号排除 |
| 单次记录中位耗时 | 成功记录的 `success.occurredAt - entry.occurredAt` P50 | 同上 | 同会话、最长 30 分钟；异常时钟样本排除并计数 |
| 7 日记录留存 | 首次成功记录用户中第 7 个自然日再次成功记录人数 / 首次成功记录人数 | `meal_record_success` | Asia/Shanghai 自然日；观察窗未闭合不入分母 |
| 搜索无结果率 | `food_search_empty` 次数 / 有效 `food_search` 次数 | 两类搜索事件 | 空关键词、超长关键词、网络失败排除 |
| 首页接口成功率 | 成功 `dashboard_request` / 全部 `dashboard_request` | 服务端监控事件 | 按接口版本和环境分层；取消请求单列 |

所有指标必须展示定义、数据源、更新时间、观察窗完整性与数据异常状态。A4 数据质量验收前，A5 页面保持阶段未开放状态。
