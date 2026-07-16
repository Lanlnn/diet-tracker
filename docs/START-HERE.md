# 从这里开始开发

## 1. 当前状态

- M0–M10 代码基线已完成并冻结在 `m10-complete`。
- 当前阶段为 E1：在本地完成真实微信登录、基础食品、饮食、运动、趋势、日历、目标和双端回归。
- 当前运行方式为电脑上的 Docker MySQL 8.0.46 + Java 17 后端，开发者工具和同一 Wi-Fi 手机访问局域网 API。
- 阿里云、staging 域名、HTTPS、微信正式体验版灰度和生产发布均推迟到项目全部完成以后。
- 当前尚缺真实 `WECHAT_SECRET` 的本地注入和 iOS/Android 实机验收结果；它们不影响无登录自动化与本地栈启动。

## 2. 第一次运行

```bash
cd /Users/z/Documents/微信小程序/diet-tracker-master
bash backend/scripts/check-e1-environment.sh
bash deploy/local/start.sh
```

健康检查：

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://192.168.3.25:8080/actuator/health
```

然后用微信开发者工具直接导入 `miniapp/`。完整配置、真实登录和真机步骤见 [本地开发手册](./LOCAL-DEVELOPMENT.md)。

## 3. 每次开发循环

1. 从 PRD、UI 图和当前里程碑确定一个可验收范围。
2. 先更新接口、数据迁移和页面状态约定，再实现代码。
3. 页面必须覆盖加载、空、错误、正常和禁用状态，并与 `design/优化版/` 对照。
4. 执行小程序测试、后端测试和本地烟测。
5. 在开发者工具普通编译，确认问题面板为 0。
6. 涉及用户主链路时，用同一测试账号在本地 MySQL 数据上对账。
7. 涉及交互或兼容性时，在同一局域网 iOS/Android 真机复测。
8. 同步更新 API、架构、阶段文档和脱敏证据。

最小验证命令：

```bash
node --test miniapp/tests/*.test.js
find miniapp -type f -name '*.js' -print0 | xargs -0 -n1 node --check
(cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home sh mvnw test)
bash backend/scripts/check-release-readiness.sh
bash deploy/local/smoke-test.sh
git diff --check
```

## 4. E1 执行顺序

- E1.0：冻结本地版本、工具、端口、AppID 和数据版本。
- E1.1：本地 MySQL、后端、开发者工具和局域网真机可达。
- E1.2：注入本地 Secret，打通真实微信登录与基础食品。
- E1.3：完成饮食、运动、趋势、日历、目标主链路。
- E1.4：完成 iOS/Android 本地回归；正式体验版灰度移交未来云端阶段。

详细退出条件见 [E1 本地功能闭环](./NEXT-DEVELOPMENT.md)。

## 5. 提交与完成

- 不提交 `.env.local`、数据库备份、Token、openid、微信 code 或个人数据。
- 自动化通过不等于真机通过，真机通过也不等于允许生产发布。
- 当前阶段只有在本地功能和双端结果完整时才算完成；云端发布不是 E1 的完成条件。
- 合并前确认 CI、文档、迁移、回滚说明和 UI 证据与同一 Git SHA 对应。
