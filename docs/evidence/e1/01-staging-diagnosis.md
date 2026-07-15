# E1.1 staging 诊断记录

诊断时间：2026-07-16 00:06–00:07 CST（2026-07-15 16:06–16:07 UTC）

## 结果

| 检查 | 实际结果 | 结论 |
| --- | --- | --- |
| `tigercloud.asia` NS | `dns29.hichina.com`、`dns30.hichina.com` | 权威 DNS 在阿里云 DNS |
| `staging.tigercloud.asia` A（1.1.1.1） | `NXDOMAIN` | 未配置公网记录 |
| `staging.tigercloud.asia` A（8.8.8.8） | `NXDOMAIN` | 未配置公网记录 |
| staging HTTPS/健康/API | 无法建立有效连接 | DNS 前置条件不成立 |
| `tigercloud.asia` A（1.1.1.1） | `8.133.176.179` | 现有正式入口地址可达 |
| 现有证书 SAN | 仅 `tigercloud.asia`、`www.tigercloud.asia` | 不覆盖 staging，不能直接复用证书 |
| 强制把 staging 解析到正式 IP | TLS 主机名校验失败；忽略 TLS 后健康为 Nginx 404 | 还缺独立证书、虚拟主机、健康反代和体验后端 |
| GitHub `master` CI | run `29425320940` 成功 | 代码基线可构建，不代表 staging 可用 |
| 本机部署权限 | 无阿里云 CLI 配置、无服务器 SSH 主机、无 GitHub 部署 Secret | 当前工作区无法执行 DNS/服务器变更 |

本次检查没有使用本机代理解析结果作为公网 DNS 证据；公网结论以两个独立递归解析器为准。

## 恢复动作

1. 域名管理员在阿里云 DNS 为 `staging.tigercloud.asia` 添加指向体验服务器的 A/AAAA 或 CNAME，TTL 建议先设为 600 秒。现有 `8.133.176.179` 只有在服务器负责人确认可承载独立体验服务后才能作为目标，不能仅凭正式 API 可达就直接复用。
2. 服务器负责人提供可部署 Java 17 服务、独立 MySQL 8 和持久化上传目录的体验主机。
3. TLS 负责人为准确主机名签发证书并安装完整证书链。
4. 反向代理开放 `/api/**` 与 `/actuator/health`，并保留 `Authorization`、`X-Request-Id`、上传 Content-Type 和客户端地址信息。
5. 微信公众平台管理员把 `https://staging.tigercloud.asia` 加入 request 与 uploadFile 合法域名。
6. 使用待验收 SHA 构建并以 `prod` Profile 部署，运行环境与发布门禁脚本。
7. DNS 生效后执行 `bash backend/scripts/check-staging-readiness.sh`；必须连续通过健康 200、未登录业务 401 和 Seed 404。

## E1.1 当前结论

`staging.tigercloud.asia` 公网记录缺失是当前第一阻塞。恢复 DNS 前，TLS、后端、真实微信登录和真机主链路均不能形成有效验收证据。
