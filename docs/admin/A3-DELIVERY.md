# A3-1 用户今日与餐次只读诊断交付记录

**状态：** 本地闭环完成

**交付分支：** `codex/a3-support-diagnostics`

**基线：** 最新 `master` / `dfa2913`（M10，V8），叠加重排后的后台 V9

**记录日期：** 2026-07-15

## 交付范围

- `users.support_ref`：持久化随机 `usr_` + 32 位十六进制标识，非空且数据库唯一；前端与管理日志不返回 openid。
- `GET /api/admin/support/users/{supportRef}`：脱敏用户资料。
- `GET /api/admin/support/users/{supportRef}/today?date=`：直接复用 `DashboardService` 和 `DashboardTodayResponse`。
- `GET /api/admin/support/users/{supportRef}/meals?date=`：数据库投影仅读取餐次保存时快照、输入量、单位、幂等键和时间。
- 三类查询仅允许 `SUPER_ADMIN`、`SUPPORT_VIEWER`，均要求 2–200 字查询原因并产生成功/失败审计。
- React 管理端启用“用户诊断”，覆盖 loading、submitting、success、empty、error、forbidden；无任何修改用户或记录的入口。

## 自动化证据

- 后端全量：Java 17；默认全量中的 MySQL 测试按环境条件跳过后另行实跑。
- A3 核心集成：同用户同日期的小程序 `/api/dashboard/today` 与管理今日接口 JSON 完全相等。
- 历史隔离：后台修改系统食品后，小程序未来计算使用新值；管理餐次接口仍返回旧快照。
- 权限：SUPPORT_VIEWER 成功，未登录 401，无关 FOOD_EDITOR 403，缺少原因 400。
- 审计：验证管理员、角色、原因、supportRef、请求 ID、结果、时间；响应与摘要均不含 openid。
- 小程序：`node --test miniapp/tests/*.test.js`，11/11 通过。
- 管理端：`npm run lint`、`npm run build` 通过，依赖审计 0 漏洞。

## MySQL 8 与浏览器证据

- 迁移顺序固定为：M10 用户目标与删除审计 V8、后台安全与审计 V9、支持诊断 V10。
- 隔离 MySQL 8 空库执行 V1–V10，并通过 Hibernate `ddl-auto=validate`。
- MySQL 集成测试验证最终版本 V10、基础食品、M10 表结构及 supportRef 格式/非空/唯一性。
- 真实 Vite + Spring Boot + MySQL 8.4 + Playwright：管理员登录、导航、精确诊断、今日指标、三大营养素、餐次快照、幂等键与空结果状态通过。
- 浏览器产生的三类成功审计及三类未找到失败审计均已核对；审计摘要 openid 泄漏计数为 0。

## 边界

- 不提供用户封禁、删除、编辑或代用户修改餐次。
- 不按昵称搜索，不在管理端重新计算今日汇总或历史餐次营养。
- 本轮仅本地提交，不推送远端。
