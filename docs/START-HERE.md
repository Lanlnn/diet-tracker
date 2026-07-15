# 从这里开始开发

这份文档说明当前交付位置，以及每次开发必须经过的验证链路。

## 1. 当前状态

- 仓库：`https://github.com/Lanlnn/diet-tracker`
- 默认分支：`master`，通过 PR 和 CI 交付
- M0 工程与安全基线：已完成
- M1 设计系统与五 Tab 骨架：已完成
- M2 登录与个人资料：已完成
- M3 食品库与搜索记录入口：已完成
- M4 热量计算：开发与本地验收完成，待 PR 合并
- 下一开发阶段：M5 饮食保存与餐次详情（必须在 M4 合并后开始）
- 微信开发者工具目录：`/Users/z/Documents/微信小程序/diet-tracker/miniapp`
- 后端目录：`/Users/z/Documents/微信小程序/diet-tracker/backend`

里程碑要求、分支名和验收清单以 [`DEVELOPMENT.md`](./DEVELOPMENT.md) 与 [`milestones/`](./milestones/) 为准。阶段之间不得跳过依赖；每个阶段都要完成代码、自动化测试、UI/开发者工具检查、文档、PR 和标签。

## 2. 首次启动

### 后端

项目统一使用 Java 17、Spring Boot、PostgreSQL、Flyway 和 Maven Wrapper。先按 [环境变量示例](../.env.example) 配置本地变量，再运行：

```bash
cd /Users/z/Documents/微信小程序/diet-tracker/backend
sh mvnw clean test
sh mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

真实数据库密码、微信 Secret 和 JWT Secret 只能保存在本地或部署平台的 Secret 管理中。如果历史凭据尚未完成轮换，应先在对应平台使旧值失效；修改仓库文本不能替代轮换。

### 小程序

微信开发者工具导入：

```text
/Users/z/Documents/微信小程序/diet-tracker/miniapp
```

确认 AppID 正确、编译问题面板为 0、Network 指向当前开发环境。开发时可临时关闭域名校验，生产配置不得依赖该选项。

## 3. 每次开发循环

开始前：

```bash
git status --short --branch
git switch master
git pull --ff-only origin master
git switch -c codex/mN-short-description
```

提交前至少执行：

```bash
cd /Users/z/Documents/微信小程序/diet-tracker
(cd backend && sh mvnw --batch-mode test)
node --test miniapp/tests/*.test.js
find miniapp -name '*.js' -not -path '*/miniprogram_npm/*' -print0 | xargs -0 -n1 node --check
git diff --check
```

同时人工确认：

- 微信开发者工具编译通过。
- 改动页面的 loading、success、empty、error、retry 和 submitting 状态按适用范围检查。
- UI 与 `design/优化版` 对应设计图完成整页对照。
- 没有提交 Secret、Token、个人数据、本机构建产物或无关文件。
- API、数据库和架构变化已同步文档与迁移。

## 4. PR 与阶段完成

1. 推送阶段分支并创建 Draft PR。
2. CI 全绿后完成里程碑清单和视觉验收记录。
3. 将 PR 标记为 Ready，合并到最新 `master`。
4. 从合并后的 `master` 创建对应 `mN-complete` 标签并推送。
5. 下一阶段必须从最新 `master` 新建分支。

不得用“本地可运行”替代 CI、开发者工具和设计验收，也不得在阶段 PR 未合并时提前创建完成标签。
