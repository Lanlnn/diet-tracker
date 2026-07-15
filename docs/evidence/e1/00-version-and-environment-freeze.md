# E1.0 版本与本地环境冻结

记录日期：2026-07-16

策略：项目全部完成前只在本地开发和联调，不实施云端部署。

## 版本

| 项目 | 冻结值 |
|---|---|
| M0–M10 基线 | `dfa2913a1be24120b7e6def5d9138fad2ccdde45` / `m10-complete` |
| E1 分支 | `codex/experience-environment-closure` |
| Java | Homebrew OpenJDK 17.0.19；项目目标 Java 17 |
| MySQL | Docker `mysql:8.0.46` |
| Flyway | V1–V8 |
| 小程序 AppID | `wx8cfee49a3f7392b2` |
| 开发者工具 | Nightly 2.02.2607142（已知最近编译基线） |

## 本地运行地址

| 用途 | 地址 |
|---|---|
| 本机健康 | `http://127.0.0.1:8080/actuator/health` |
| 本机 API | `http://127.0.0.1:8080/api` |
| 当前局域网健康 | `http://192.168.3.25:8080/actuator/health` |
| 小程序 develop/trial 标识 | `http://192.168.3.25:8080/api` |
| 正式 release | 保留未来 HTTPS 地址，当前不部署、不验收 |

局域网地址不是永久配置。换网络后在本地存储设置 `apiBaseUrl`，不得为了临时 IP 修改未来正式 release 地址。

## 配置来源

| 配置 | 来源 | 状态 |
|---|---|---|
| 数据库 URL/账号/密码 | `deploy/local/compose.yml` 的仅本地容器网络 | 已提供，不对公网暴露 MySQL |
| `WECHAT_APPID` | `miniapp/project.config.json` | 已冻结 |
| `WECHAT_SECRET` | Git 忽略的 `deploy/local/.env.local` | 待用户本地填写真实值 |
| JWT / 删除审计 pepper | `.env.local` | 示例值可启动；真实登录验收前应换成本地随机值 |
| 上传目录 | Docker `avatar-data` 卷 | 已定义 |
| Profile | `local` | 已定义 |

## 已验证与待验证

- 已验证：Java 17、MySQL 8.0.46、Docker 可用，AppID 与项目配置一致。
- 已验证：此前同一代码基线的 Flyway V8、7 个分类、48 个系统食品、健康 200 和未登录 401。
- 本轮本地 Compose 的实际启动结果记录在 `01-local-runtime.md`。
- 待验证：真实微信 Secret、iOS/Android 同网联调和 E1.3 用户主链路。

E1.0 退出标准：本地环境检查和 Compose 配置通过；真实 Secret 不进入 Git。云端配置不属于退出条件。
