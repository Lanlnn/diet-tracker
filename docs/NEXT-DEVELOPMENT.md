# E1：本地真实功能与双端闭环

**状态：** 进行中

**策略：** 全部功能先在本地试跑，项目完成后再考虑云端部署

**执行依据：** 优化版 PRD、`design/优化版/` UI、API 和开发规范

## 1. 阶段目标

在不依赖阿里云、域名和 HTTPS 的条件下，让开发者工具以及同一局域网的 iOS/Android 连接本地 Java 17 + MySQL 8 后端，使用真实微信登录完成完整用户主链路。

E1 完成只代表“本地功能与双端质量闭环”。微信正式体验版上传、合法域名配置和线上灰度属于项目完成后的独立发布阶段。

## 2. E1.0：冻结版本与环境配置

执行：

- 基线固定为 `m10-complete`；E1 变更在 `codex/experience-environment-closure`。
- Java 17、MySQL 8.0.46、Flyway V8、后端端口 8080。
- 小程序 AppID 固定为 `wx8cfee49a3f7392b2`。
- 当前电脑局域网地址固定记录为 `192.168.3.25`；换网络时通过 `apiBaseUrl` 本地覆盖。
- Secret 只进入 Git 忽略的 `deploy/local/.env.local`。

退出条件：环境检查脚本通过；版本和配置来源可追溯；仓库无真实 Secret。

## 3. E1.1：本地后端与局域网可达

执行：

1. 运行 `bash deploy/local/start.sh`。
2. 本机和局域网健康接口均返回 200/UP。
3. Flyway 最高版本为 V8，分类为 7 条，系统食品为 48 条。
4. 未登录业务接口返回结构化 401 和 requestId。
5. 开发者工具直接打开 `miniapp/` 并完成普通编译。
6. iOS/Android 浏览器能访问局域网健康接口。

退出条件：本地栈可以重复停止、启动并保留数据；两台手机至少能到达后端。此步骤不要求真实微信 Secret。

## 4. E1.2：真实微信登录与基础食品

执行：

1. 在 `.env.local` 写入与 AppID 匹配的真实 `WECHAT_SECRET`，重启后端。
2. 执行 `bash backend/scripts/check-e1-environment.sh --require-wechat`，确认本地配置就绪且无 Secret 输出。
3. 真机执行首次 `wx.login → /api/auth/login`，获得业务 Token。
4. 关闭重开小程序，验证再次登录和用户身份稳定。
5. 模拟 Token 失效，验证并发 401 只触发一次重登。
6. 搜索米饭、鸡胸肉、鸡蛋、牛奶、西兰花等基础食品。
7. 验证系统食品所有用户可见，自定义食品只对创建者可见。

退出条件：真实登录无 P0/P1；食品分类和 48 条系统食品可检索、可计算、可记录。

## 5. E1.3：用户主链路

按同一用户、同一数据库、同一 Git SHA 依次验证：

1. 设置当前体重、目标体重和每日热量目标。
2. 搜索食品并预览不同份量的营养计算。
3. 分别新增早餐、午餐、晚餐或加餐。
4. 编辑份量、餐次和时间，再删除一条记录。
5. 今日首页与餐次详情的摄入热量、宏量营养一致。
6. 新增、编辑、删除运动；首页净热量与运动页一致。
7. 趋势页 7/30/90 天数据与饮食、运动记录一致。
8. 日历月摘要、选中日详情和历史日期记录一致。
9. 修改目标后，首页、趋势、日历和个人中心统一使用新目标。
10. 创建自定义食品并记录，确认用户隔离。
11. 验证资料修改、头像上传失败状态和账号删除二次确认。

退出条件：主链路无 P0/P1；跨页数据误差符合 PRD；刷新、返回、重进后状态正确。

## 6. E1.4：iOS/Android 本地回归

当前执行的是同一 Wi-Fi 下的真机调试/预览，不是微信后台上传后的正式体验版灰度。

至少覆盖：

- iOS 一台、Android 一台；记录机型、系统、微信版本和网络。
- 冷启动、首次登录、再次登录、断网恢复、401 重登。
- 食品搜索、饮食增删改、运动增删改、趋势切换、日历跨月、目标修改。
- 空数据、加载、接口错误、长文本、键盘遮挡、安全区和弱网重复提交。
- 页面与 `design/优化版/` 对照，核心点击区域不小于 88rpx。

退出条件：双端无 P0/P1，P2 有明确记录；验收表与脱敏截图完整。

## 7. 质量门禁

```bash
node --test miniapp/tests/*.test.js
find miniapp -type f -name '*.js' -print0 | xargs -0 -n1 node --check
(cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home sh mvnw test)
bash backend/scripts/check-release-readiness.sh
bash backend/scripts/check-e1-environment.sh
bash deploy/local/smoke-test.sh
node deploy/local/main-flow-test.mjs
git diff --check
```

P0：无法启动、登录或保存，数据损坏/越权，核心口径错误。立即停止验收。

P1：核心步骤稳定失败、双端关键操作不可用、重复写入。修复后全链路重测。

P2：局部视觉或异常状态问题，不阻断数据正确性，必须记录负责人和复测结果。

## 8. 证据与安全

每轮记录 Git SHA、时间、设备、系统、微信版本、步骤、预期、实际、状态码和 requestId。不得记录 Token、openid、微信 code、Secret、密码或真实个人数据。

执行记录见：

- `evidence/e1/00-version-and-environment-freeze.md`
- `evidence/e1/01-local-runtime.md`
- `evidence/e1/02-real-device-acceptance.md`
- `evidence/e1/03-ui-coverage-audit.md`

## 9. 后续云端阶段

用户确认项目全部完成后再单独规划：云厂商选择、staging/production 拓扑、域名、HTTPS、微信合法域名、数据库备份、体验版上传、5%→25%→100% 灰度、监控和生产回滚。E1 不提前实施这些工作。
