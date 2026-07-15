# 下一阶段开发：体验环境与真机功能闭环

**阶段代号：** E1

**状态：** 进行中；E1.0 已建立冻结记录，E1.1 阻塞于 staging 公网 DNS

**前置条件：** M0–M10 代码已合入 `master`，`m10-complete` 已指向合并提交

**阶段目标：** 让微信体验版连接持续可用的后端，使用真实微信登录和真实 MySQL 数据完成一条可重复的用户主链路

**不包含：** 新增产品功能、管理后台 A3/A4、视觉重构、用 Mock 数据代替真实接口

当前执行证据：

- [`E1.0 版本与环境冻结`](./evidence/e1/00-version-and-environment-freeze.md)
- [`E1.1 staging 诊断`](./evidence/e1/01-staging-diagnosis.md)
- [`E1.2–E1.4 真实链路验收表`](./evidence/e1/02-real-device-acceptance.md)
- [`staging 可重复部署与回滚`](../deploy/staging/README.md)

## 1. 为什么现在优先做这个阶段

M0–M10 的页面、接口和自动化已经存在，但“代码完成”不等于“体验版可用”。2026-07-15 核查发现：

- 小程序体验版 `trial` 环境配置为 `https://staging.tigercloud.asia/api`。
- 当时 `staging.tigercloud.asia` 没有可用 DNS 解析，HTTPS 连接失败。
- 体验版因此无法完成 `wx.login → /api/auth/login`，也无法取得业务 Token。
- 登录失败后，食品搜索、记录、今日、运动、趋势、日历和个人中心都不能形成真实功能闭环。
- `https://tigercloud.asia/api` 能返回受保护 API 的 `401`，只能证明线上反向代理/API 存在，不能替代独立体验环境验收。

因此当前真实交付状态是：**M0–M10 代码基线完成，体验环境与真实用户链路未完成。** 本阶段完成前，不把“编译通过”“模拟器截图”或“风险豁免”当作真机功能验收。

## 2. 完成定义

本阶段只有同时满足以下条件才算完成：

- `staging.tigercloud.asia` 可稳定解析，HTTPS 证书、证书链和域名匹配正确。
- 体验环境后端以 `prod` 安全配置运行，健康检查可用，Seed/调试接口不可用。
- 后端使用与当前小程序 AppID 匹配的微信凭据，真机首次登录和再次登录均成功。
- MySQL 8 Flyway 版本与当前代码一致，系统分类和系统食品存在。
- 新用户能从零完成“搜索食品 → 计算 → 保存一餐 → 今日更新”。
- 同一用户能继续完成运动记录、趋势、日历、目标修改和重新进入后的数据恢复。
- iOS 和 Android 至少各完成一次主链路；发现的 P0/P1 全部关闭。
- 关键步骤有脱敏证据，CI、真机、接口和数据库证据能够对应同一提交和同一环境。

## 3. 开发顺序

必须按 E1.0 → E1.4 执行。前一步未达到退出条件时，不进入下一步，也不并行新增产品功能。

### E1.0：冻结版本与建立环境清单

目标：明确“正在验证哪一版、部署到哪里、由什么配置驱动”。

实施项：

- 从最新 `master` 创建 `codex/experience-environment-closure` 分支。
- 记录待部署 Git SHA、构建时间、后端镜像/制品版本和小程序体验版版本号。
- 确认小程序 AppID 为 `miniapp/project.config.json` 中的当前项目 AppID。
- 建立体验环境配置清单，只记录变量名、来源和是否已配置，不把真实值写入仓库。

必需 Secret/配置：

| 配置 | 要求 |
| --- | --- |
| `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` | 指向独立 MySQL 8 体验库，不复用个人本地库 |
| `WECHAT_APPID`、`WECHAT_SECRET` | 与上传体验版的小程序 AppID 完全匹配 |
| `JWT_SECRET` | 至少 32 个随机字节，不能使用示例值 |
| `DELETION_AUDIT_PEPPER` | 独立 Secret；不依赖可轮换的 JWT 更安全 |
| `APP_BASE_URL` | 体验环境公开 HTTPS 地址 |
| `UPLOAD_DIR` | 持久化且进程可写，不使用临时构建目录 |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `RATE_LIMIT_ENABLED` | `true` |

退出条件：版本、域名、数据库和 Secret 负责人明确；仓库及构建日志没有泄露真实值。

### E1.1：恢复体验域名和后端

目标：让真机可以通过 HTTPS 稳定访问体验 API。

实施项：

1. 为 `staging.tigercloud.asia` 配置 A/AAAA 或 CNAME，并确认公网递归 DNS 可解析。
2. 部署与待验收 SHA 对应的 Java 17 后端。
3. 配置 TLS 证书及完整证书链，反向代理 `/api/**` 和 `/actuator/health`。
4. 确认代理保留 `Authorization`、`X-Request-Id`、上传 Content-Type 和客户端地址信息。
5. 在微信公众平台配置以下合法域名：
   - request 合法域名：`https://staging.tigercloud.asia`
   - uploadFile 合法域名：`https://staging.tigercloud.asia`
6. 不依赖开发者工具的“关闭域名校验”选项完成验证。

检查命令：

```bash
dig +short staging.tigercloud.asia A
curl --fail --show-error --max-time 10 \
  https://staging.tigercloud.asia/actuator/health
curl --include --show-error --max-time 10 \
  'https://staging.tigercloud.asia/api/foods/search?keyword=鸡胸'
```

预期：

- DNS 返回预期地址。
- 健康检查返回 HTTP 200 和 `UP`。
- 未登录业务请求返回结构化 401，而不是 DNS、TLS、Nginx 404 或 HTML 错误页。
- `/api/setup/seed` 在体验环境返回 404。

退出条件：连续多次检查无偶发 DNS/TLS/5xx，真机 Network 能到达相同域名。

### E1.2：打通微信登录与初始数据

目标：新用户首次进入后能拿到 Token，并拥有可开始记录的基础数据。

实施项：

1. 真机执行 `wx.login`，把一次性 code 发送至 `POST /api/auth/login`。
2. 后端使用真实微信 `jscode2session` 换取 openid；禁止在体验环境加入固定 openid 或万能 code。
3. 验证首次登录创建用户和默认 `UserGoal`，重复登录不创建重复用户。
4. 确认响应和日志不包含 openid、微信 Secret、JWT 全文或一次性 code。
5. 在体验 MySQL 空库执行 Flyway V1–V8；V6 必须提供系统分类和基础食品。
6. 通过只读 SQL 或受保护诊断确认分类、系统食品、用户和目标数量。

数据库核对示例：

```sql
SELECT version, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT COUNT(*) AS category_count FROM food_category;
SELECT COUNT(*) AS system_food_count
FROM food_item
WHERE user_id IS NULL;

SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS goal_count FROM user_goal;
```

预期：

- Flyway 最终版本为当前 `master` 对应版本，迁移全部成功。
- 系统食品数量大于 0；搜索“鸡胸”等基础词能返回结果。
- 新用户登录后存在且仅存在一份目标记录。
- 清除本地 Token 后再次登录仍能恢复同一用户数据。

退出条件：真机登录、刷新登录、基础食品搜索均成功；失败时页面展示可理解的错误与重试入口。

### E1.3：完成真实用户主链路

目标：证明功能不是静态页面，而是同一用户、同一数据库、同一套真实接口驱动的闭环。

使用一个全新测试账号，按顺序执行：

1. 登录并补充昵称/头像或跳过非必需资料。
2. 搜索一个系统食品，分别检查正常、无结果和网络错误状态。
3. 输入 150g，核对热量和三大营养素计算。
4. 保存到早餐；防重复点击不得产生两条记录。
5. 返回今日页，核对早餐、总摄入、营养进度和剩余可摄入。
6. 编辑并删除一条餐次记录，今日聚合随之更新。
7. 添加一次运动，核对今日运动与净摄入；运动热量不得增加“剩余可摄入”。
8. 创建一条自定义食品并记录，确认只对当前用户可见。
9. 打开 7/30/90 天趋势；数据不足状态应准确，不能伪造趋势数据。
10. 打开饮食日历，核对记录日期、选中日期和餐次明细。
11. 修改热量、营养和体重目标，返回首页与趋势核对新目标。
12. 杀掉小程序并重新进入，确认登录态和服务端数据恢复。

每一步至少保留：时间、体验版版本、Git SHA、设备/系统、接口状态码、requestId、预期结果、实际结果和脱敏截图。不得保存 Token、openid、微信 code 或个人敏感原文。

退出条件：主链路全部通过；跨页面聚合一致；无 P0/P1 未关闭缺陷。

### E1.4：双端回归与体验版灰度

目标：把一次成功升级为可重复的发布候选证据。

最低设备矩阵：

| 平台 | 最低覆盖 | 必测内容 |
| --- | --- | --- |
| iOS | 1 台常见屏幕 | 登录、搜索、保存、首页、运动、趋势、日历、目标 |
| Android | 1 台常见屏幕 | 同上，并检查返回键、键盘遮挡和权限失败 |

发布观察：

- 先内部体验账号，再扩大体验范围；不直接发布正式版。
- 观察 API 5xx、401、429、P95、数据库连接池、Flyway 和磁盘/上传目录。
- 记录至少 30 分钟稳定观察窗口。
- 登录或饮食记录主链路不可用、数据串用户、计算错误时立即停止验证并回滚。

退出条件：双端主链路通过，监控稳定，回滚路径可执行，形成是否进入正式审核的明确结论。

## 4. 缺陷优先级

| 级别 | 示例 | 处理要求 |
| --- | --- | --- |
| P0 | 登录完全不可用、数据串用户、保存造成数据损坏、账号误删 | 立即停止体验，优先回滚，修复并全链路复测 |
| P1 | 搜索无系统食品、无法保存、首页数据错误、核心页面持续白屏 | 阻止阶段完成，修复后执行相关链路与回归 |
| P2 | 单个异常状态缺失、局部布局阻碍操作、非核心入口失效 | 明确负责人和完成日期，可在灰度前修复 |
| P3 | 文案、轻微间距或不影响任务的视觉问题 | 记录后排期，不得冒充功能阻塞 |

缺陷记录模板：

```text
标题：
Git SHA / 体验版版本：
设备与系统：
账号：仅写脱敏测试编号
前置数据：
复现步骤：
预期结果：
实际结果：
接口状态码 / requestId：
截图或日志路径：
严重级别：P0 / P1 / P2 / P3
处理状态：未处理 / 修复中 / 待复测 / 已关闭
```

## 5. 自动化与提交门禁

环境修复不能降低既有代码质量。提交前仍需运行：

```bash
(cd backend && sh mvnw --batch-mode test)
node --test miniapp/tests/*.test.js
find miniapp -name '*.js' -not -path '*/miniprogram_npm/*' \
  -print0 | xargs -0 -n1 node --check
git diff --check
```

本阶段 PR 必须包含：

- 环境或配置代码变更，但不包含真实 Secret。
- 真实登录和业务主链路发现问题的修复。
- 对应自动化回归测试。
- 脱敏验收记录，以及仍未解决的风险。
- 回滚方法和体验环境恢复方法。

## 6. 与后续开发的关系

- A3 管理后台分支保留，不在体验主链路修复过程中混入本阶段 PR。
- E1 完成并合入后，再评审和合并 A3。
- A4 埋点与产品质量数据基础必须在真实业务链路稳定后开始，否则只能采集失败流量或产生误导指标。
- 新产品功能必须等待 E1 完成；当前优先让已有功能真正可用。
