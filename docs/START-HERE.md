# 从这里开始开发

这份文档回答两个问题：现在先做什么，以及每天如何安全地开发、验证和推送。

## 1. 当前项目状态

- 正确仓库：`https://github.com/Lanlnn/diet-tracker`
- 默认分支：`master`，已开启 PR 和 CI 保护
- 当前基线分支：`codex/m0-m10-development-plan`
- 当前交付：[草稿 PR #1](https://github.com/Lanlnn/diet-tracker/pull/1)
- 当前阶段：M0 与 M1 已完成代码实现和本地验证，状态为“待验收”
- 下一开发阶段：验收 M0/M1 后进入 [M2 登录与个人资料](./milestones/M2-auth-profile.md)
- 微信开发者工具项目目录：`/Users/z/Documents/微信小程序/diet-tracker/miniapp`
- 后端目录：`/Users/z/Documents/微信小程序/diet-tracker/backend`

M0/M1 的实现位于当前工作区；验收前不要创建完成标签，也不要继续叠加 M2 业务功能。

## 2. 今天必须先完成的三件事

### 2.1 立即轮换已经暴露的凭据

当前 Git 历史中的后端配置曾包含可用形式的默认数据库密码、微信 Secret 和 JWT Secret。仓库是公开的，应将这些凭据视为已经泄露。

按顺序处理：

1. 在数据库或云服务控制台修改数据库密码，旧密码立即失效。
2. 在微信公众平台重置小程序 Secret，确认旧 Secret 失效。
3. 生成新的随机 JWT Secret，长度至少 32 字节。
4. 新凭据只保存在本机环境变量和部署平台的 Secret 管理中。
5. 不要把新值写入 Markdown、聊天记录、截图、提交信息或 Git 文件。

仅修改 `application.properties` 或删除 Git 历史中的文本，不能替代凭据轮换。

### 2.2 审核并合并开发基线

打开 [PR #1](https://github.com/Lanlnn/diet-tracker/pull/1)：

1. 确认优化版 8 张 UI、PRD、M0–M10 文档和目录重构都属于项目。
2. 确认 `Miniapp static checks` 与 `Backend tests` 为绿色。
3. 把 Draft 改为 Ready for review。
4. 使用 Squash and merge 合并到 `master`。
5. 不创建 `m0-complete` 标签；当前只完成规划，不代表 M0 已完成。

### 2.3 创建真正的 M0 开发分支

PR #1 合并后执行：

```bash
cd /Users/z/Documents/微信小程序/diet-tracker
git switch master
git pull --ff-only origin master
git switch -c codex/m0-foundation
```

不要从当前规划分支继续叠加 M0 代码。

## 3. M0 的开发顺序

### M0.1 环境和密钥治理

目标：仓库内没有真实凭据，新电脑可以根据示例配置启动。

需要完成：

- 把数据库密码、微信 AppID/Secret、JWT Secret、上传地址改为必填环境变量。
- 增加不含真实值的 `.env.example` 或环境变量说明。
- 拆分 `application-local.properties`、`application-test.properties`、`application-prod.properties`。
- 删除敏感日志和重复的 DEBUG 配置。
- 增加密钥扫描，并让 CI 在发现真实凭据时失败。

验收：缺少必要环境变量时明确启动失败；Git 和 CI 日志中看不到 Secret、Token 或数据库密码。

### M0.2 统一 Java 与数据库

目标：本机、CI 和部署使用同一技术栈。

建议基线：

- Java 17
- Spring Boot 3.4.x
- PostgreSQL
- Maven Wrapper

需要完成：

- `pom.xml` 只声明 Java 17，删除 Java 25 与 17 并存的配置。
- 删除 MySQL 驱动和 MySQL 专用配置。
- 修改 `start.sh`，删除机器专用绝对路径，统一使用 `sh mvnw`。
- 文档、CI 和部署配置使用相同 Java 版本。

验收：全新环境执行 `sh mvnw clean test` 成功。

### M0.3 建立 PostgreSQL 迁移

目标：数据库结构由代码版本管理，不再依赖 Hibernate 自动改表。

需要完成：

- 引入 Flyway。
- 建立第一版基线迁移和必要索引。
- 将 `ddl-auto` 从 `update` 改为可控配置，生产环境使用 `validate`。
- 明确 `data.sql` 只在哪个 Profile 运行。
- 在本地空数据库完整执行迁移。

验收：删除本地测试库后，可以仅靠迁移脚本恢复结构。

### M0.4 后端接口基线

目标：后续 M2–M10 不再直接暴露实体或各自定义错误格式。

需要完成：

- Request/Response DTO。
- Jakarta Validation 参数校验。
- 统一异常处理与 `code/message/requestId/fieldErrors` 响应。
- 鉴权上下文和用户隔离检查。
- 关闭生产环境 Seed/诊断接口。
- 收紧头像上传的类型、大小、文件名和访问路径。

验收：非法参数、未登录、无权限、数据不存在和系统异常都有稳定错误码。

### M0.5 小程序请求基线

目标：页面只处理展示和交互，请求、登录和错误处理保持一致。

需要完成：

- 把固定 API 地址改为开发、测试、生产环境配置。
- 建立唯一登录 Promise。
- 多个请求同时遇到 401 时只刷新一次登录。
- 页面区分 `loading / success / empty / error / submitting`。
- 删除 Token、openid、用户资料和完整响应日志。

验收：首次启动不会并发登录；断网显示错误和重试，不显示伪造的空数据。

### M0.6 测试和最终验收

至少增加：

- 后端上下文或核心 Service 测试。
- 配置缺失测试。
- 用户数据隔离测试。
- 小程序登录互斥和 401 重试测试。
- CI 密钥扫描。

完成全部 [M0 验收项](./milestones/M0-foundation.md) 后，才把阶段状态改为“待验收”。

## 4. 本地开发环境

### 4.1 必备软件

- 微信开发者工具
- Git 与 GitHub CLI
- Java 17
- PostgreSQL
- Node.js 22 或更高版本，仅用于静态检查和后续前端测试

### 4.2 打开小程序

微信开发者工具选择“导入项目”，项目目录必须是：

```text
/Users/z/Documents/微信小程序/diet-tracker/miniapp
```

不要打开 `/Users/z/Documents/微信小程序` 或仓库根目录。导入后先确认：

- AppID 属于当前小程序。
- 编译没有 `app.json` 页面缺失错误。
- Network 请求指向当前选择的开发环境。
- 开发阶段可以临时关闭域名校验，提交和生产配置不能依赖该选项。

### 4.3 启动后端

M0.1 完成并配置本地环境变量后，在终端运行：

```bash
cd /Users/z/Documents/微信小程序/diet-tracker/backend
sh mvnw clean test
sh mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

在凭据轮换和本地 Profile 完成前，不建议继续连接当前公开配置中的远程数据库。

## 5. 每天的开发循环

开始工作：

```bash
git status --short --branch
git pull --ff-only
```

完成一个清晰改动后：

```bash
git diff
git diff --check
sh backend/mvnw --batch-mode test
git add 需要提交的明确路径
git diff --cached
git commit -m "chore(m0): describe the change"
git push
```

同时完成以下人工检查：

- 微信开发者工具编译通过。
- 改动页面的正常、加载、空、错误和提交状态已检查。
- 没有提交新凭据、本机路径、构建产物、ZIP、DOCX 或旧设计归档。
- API、数据库或架构变化已经同步文档。

## 6. M0 完成后的动作

1. 把 `codex/m0-foundation` PR 从 Draft 改为 Ready for review。
2. 确认自动检查、开发者工具和 M0 验收全部通过。
3. 合并到 `master`。
4. 从最新 `master` 创建并推送 `m0-complete` 标签。
5. 从最新 `master` 创建 `codex/m1-design-system-shell`。
6. M1 才开始实现颜色、字体、间距、圆角、阴影、通用状态和五 Tab 应用骨架。

## 7. 遇到不确定事项时

- 产品规则：先看 [PRD](./饮食与运动小程序-产品需求文档-PRD-v1.0.md)。
- 页面和视觉：只看 [`design/优化版`](../design/优化版/页面索引.md)。
- 当前阶段边界：看 [M0–M10 里程碑](./milestones/README.md)。
- Git 操作：看 [Git 与 GitHub 交付规范](./git-workflow.md)。
- API 行为：看 [API 文档](./api.md)，并以后端代码为最终依据。

如果一个决定会改变数据模型、认证、安全或多个阶段，应先写 ADR 或更新 PRD，再写代码。
