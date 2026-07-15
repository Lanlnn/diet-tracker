# M0–M10 复盘环境

- 日期：2026-07-15（Asia/Shanghai）
- 分支：`codex/m10-profile-release`
- 验收工作树基线：`b34c49f63f9079384641050392881b144176e20c` 加本文件所述收口改动
- Java：Homebrew OpenJDK 17.0.19
- Maven 编译目标：release 17
- Node.js：v24.16.0
- MySQL：Docker `mysql:8.0`，服务端 8.0.46，使用一次性测试库
- 微信开发者工具：Nightly 2.02.2607142

## 验证结果

- 后端真实 MySQL 8 完整测试：38/38 通过，0 失败，0 错误，0 跳过。
- 后端 H2 快速测试：所有适用测试通过；MySQL 迁移和性能专项按环境条件跳过。
- 小程序：12/12 通过。
- JavaScript 与 JSON 静态检查：通过。
- 发布准备脚本：通过。
- `git diff --check`：通过。
- 旧 MySQL 8 数据迁移：自动备份、V5 接管、V6–V8 增量迁移和行数核对通过。
- API 性能：40 次采样 P95，首页 10ms、搜索 8ms、月历 18ms、90 天趋势 18ms。
- 监控告警：Prometheus 6 条规则通过 `promtool check rules`。
- 微信开发者工具：M10 当前工作树 GUI 普通编译问题面板 0；个人中心与隐私页截图已留存。编译后首页显示网络错误，因为验收时未启动本地后端。

本文件不包含密码、Token、openid 或真实健康数据。最终交付提交以本分支 Git 历史为准。
