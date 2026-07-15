# M0–M10 复盘环境

- 日期：2026-07-15（Asia/Shanghai）
- 分支：`codex/m10-profile-release`
- 验收工作树基线：`9fabb6eab7883cd92e02f9322caaa6e44eb5953c`
- Java：Amazon Corretto 18.0.2（本机唯一 JDK）
- Maven 编译目标：release 18
- Node.js：v24.16.0
- MySQL：Docker `mysql:8.0`，服务端 8.0.46，使用一次性测试库
- 微信开发者工具：Nightly 2.02.2607142

## 验证结果

- 后端：37/37 通过，0 失败，0 错误，0 跳过。
- 小程序：12/12 通过。
- JavaScript 与 JSON 静态检查：通过。
- 发布准备脚本：通过。
- `git diff --check`：通过。
- 旧 MySQL 8 数据迁移：自动备份、V5 接管、V6–V8 增量迁移和行数核对通过。
- 微信开发者工具：M10 普通编译问题面板 0；个人中心与隐私页截图已留存。当前工作树 CLI 复编译被“服务端口关闭”设置阻止，没有修改用户安全设置。

本文件不包含密码、Token、openid 或真实健康数据。最终交付提交以本分支 Git 历史为准。
