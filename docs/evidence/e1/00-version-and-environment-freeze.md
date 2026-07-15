# E1.0 版本与环境冻结

- 冻结时间：2026-07-16 00:06 CST
- 仓库：`Lanlnn/diet-tracker`
- 基线分支：`master`
- 基线 Git SHA：`dfa2913a1be24120b7e6def5d9138fad2ccdde45`
- 基线标签：`m10-complete`
- 基线 CI：GitHub Actions run `29425320940`，结论 `success`
- E1 工作分支：`codex/experience-environment-closure`
- 小程序 AppID：`wx8cfee49a3f7392b2`
- 体验 API：`https://staging.tigercloud.asia/api`
- 正式 API：`https://tigercloud.asia/api`

## 制品冻结

| 项目 | 冻结值 | 状态 |
| --- | --- | --- |
| 待部署提交 | E1 分支合并后的 `master` SHA | 待 E1 PR 合并后填写 |
| 后端制品 | 必须由待部署 SHA 构建，记录文件校验和或镜像 digest | 未构建 |
| 构建时间 | 使用 UTC ISO-8601 | 未构建 |
| 小程序体验版版本号 | 上传时记录 | 未上传 |
| 小程序上传者与时间 | 微信公众平台审计记录 | 未上传 |

在上表四项未补齐前，不得把任何现有服务器进程或体验版称为本次 E1 发布候选。

## 配置清单

只记录来源和状态，不记录值。所有 Secret 必须由部署平台 Secret 管理注入。

| 配置 | 来源/负责人 | 当前状态 | 验证规则 |
| --- | --- | --- | --- |
| `DB_URL` | 体验环境部署负责人 | 未提供 | 独立 MySQL 8，不指向 localhost |
| `DB_USERNAME` | 体验数据库负责人 | 未提供 | 最小权限账号 |
| `DB_PASSWORD` | 体验数据库 Secret | 未提供 | 不进入仓库或构建日志 |
| `WECHAT_APPID` | 微信小程序项目 | 已确认变量契约 | 必须等于项目 AppID |
| `WECHAT_SECRET` | 微信公众平台管理员 | 未提供 | 与 AppID 配对，必要时先轮换 |
| `JWT_SECRET` | 部署平台 Secret | 未提供 | 至少 32 个随机字符 |
| `DELETION_AUDIT_PEPPER` | 部署平台独立 Secret | 未提供 | 至少 32 个随机字符且不同于 JWT |
| `APP_BASE_URL` | 体验域名负责人 | 已冻结契约 | `https://staging.tigercloud.asia` |
| `UPLOAD_DIR` | 服务器存储负责人 | 未提供 | 绝对持久化路径，服务账号可写 |
| `CORS_ALLOWED_ORIGINS` | 后端部署负责人 | 未提供 | 明确白名单，不使用宽泛生产值 |
| `SPRING_PROFILES_ACTIVE` | 后端部署负责人 | 已冻结契约 | `prod` |
| `RATE_LIMIT_ENABLED` | 后端部署负责人 | 已冻结契约 | `true` |

部署前在注入真实环境变量的同一服务账号下运行：

```bash
bash backend/scripts/check-e1-environment.sh
bash backend/scripts/check-release-readiness.sh
```

两个脚本只输出变量名和规则结果，不输出 Secret 值。

## E1.0 结论

- 代码、标签、CI、AppID、目标域名和配置规则已冻结。
- 本地质量基线已通过：Java 17 + MySQL 8.0.46 全量 38/38（无跳过）、小程序 12/12、Flyway V1–V8、JS/JSON 语法、发布门禁和环境门禁。
- MySQL 8 MockMvc 性能基线 P95：今日 12ms、食品搜索 14ms、日历 15ms、趋势 16ms。
- staging 容器烟测已通过：Java 17 运行时镜像、MySQL 8.0.46、prod Profile、健康 200、未登录 401、Seed 404、Flyway V8、7 个分类和 48 个系统食品；后端非 root 且根文件系统只读，MySQL 未映射公网端口。
- 真实配置来源/负责人、后端制品和体验版版本尚未就绪。
- 当前不满足 E1.0 退出条件，不能进入正式的 E1.2 真机登录验收。
