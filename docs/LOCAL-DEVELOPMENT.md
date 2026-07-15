# 本地开发与联调手册

当前项目采用“本地完成、云端后置”的开发策略。E1 阶段不申请阿里云主机、不配置域名和 HTTPS，也不把正式体验版灰度作为功能开发前置条件。MySQL 8、Java 17 后端、小程序开发者工具以及同一局域网内的 iOS/Android 真机均在本地联调。

## 1. 固定环境

| 项目 | 当前冻结值 |
|---|---|
| Git 基线 | `m10-complete`，提交 `dfa2913a1be24120b7e6def5d9138fad2ccdde45` |
| 开发分支 | `codex/experience-environment-closure` |
| Java | 17 |
| MySQL | 8.0.46（Docker） |
| 后端端口 | `8080`，监听 `0.0.0.0` 供本机和局域网访问 |
| 本机 API | `http://127.0.0.1:8080/api` |
| 当前局域网 API | `http://192.168.3.25:8080/api` |
| 小程序 AppID | `wx8cfee49a3f7392b2` |

局域网 IP 由路由器分配，换网络后可能变化。先在 `deploy/local/.env.local` 把 `LOCAL_LAN_IP` 改为新地址并重启后端，保证头像等完整 URL 正确；再在开发者工具 Console 执行以下命令覆盖 develop/trial 标识的 API 地址，然后重新编译：

```javascript
wx.setStorageSync('apiBaseUrl', 'http://新的局域网IP:8080/api')
```

清除覆盖值：

```javascript
wx.removeStorageSync('apiBaseUrl')
```

正式版 `release` 不读取本地覆盖，避免把局域网地址带入未来生产版本。

## 2. 首次启动

前置条件只有 Docker Desktop、Java 17、微信开发者工具和同一 Wi-Fi。无需在宿主机安装 MySQL。

```bash
cd /Users/z/Documents/微信小程序/diet-tracker-master
bash backend/scripts/check-e1-environment.sh
bash deploy/local/start.sh
```

`start.sh` 会执行完整后端测试和打包、构建 Java 17 容器、启动 MySQL 8.0.46、执行 Flyway V1–V8，并检查健康接口、未登录 401、7 个分类和 48 个系统食品。

验证地址：

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://192.168.3.25:8080/actuator/health
```

微信开发者工具直接导入 `miniapp/`。项目配置当前允许开发阶段关闭合法域名校验；这只用于本地开发，不是未来发布配置。

首次导入时选择“不使用云服务”。若开发者工具询问是否信任并运行项目，需要由工作区所有者亲自确认；未确认前只能检查源码，不能把模拟器画面记为本轮编译证据。

## 3. 真实微信登录

页面、基础食品和无需真实微信凭据的接口可以先启动。要验证 `wx.login`，复制本地配置并填写与 AppID 匹配的真实 Secret：

```bash
cp deploy/local/.env.local.example deploy/local/.env.local
chmod 600 deploy/local/.env.local
```

修改 `deploy/local/.env.local` 中的 `WECHAT_SECRET`；换网络时同步修改 `LOCAL_LAN_IP`。必要时轮换本地 JWT 和审计 pepper，再重新运行 `bash deploy/local/start.sh`。该文件已被 Git 忽略。不要把 Secret 发到聊天、截图、日志、Issue 或 PR。

重启前可执行以下检查。它只判断本地文件权限、AppID 一致性和 Secret 是否仍为占位值，不会输出 Secret，也不能代替真机 `wx.login`：

```bash
bash backend/scripts/check-e1-environment.sh --require-wechat
```

登录链路为：

```text
真机 wx.login → 局域网 HTTP → 本地后端 → 微信 code2Session 公网接口
```

因此电脑仍需能访问微信公网接口，但不需要任何云服务器。

## 4. 真机联调

1. 电脑和手机连接同一 Wi-Fi，关闭会隔离局域网的访客网络。
2. macOS 防火墙允许 Docker/后端接收入站连接。
3. 手机浏览器先访问 `http://192.168.3.25:8080/actuator/health`，确认返回 `UP`。
4. 在开发者工具使用真机调试或预览；本地阶段允许“不校验合法域名、web-view、TLS 版本及 HTTPS 证书”。
5. 按 `evidence/e1/02-real-device-acceptance.md` 记录 iOS、Android 结果。

如果路由器开启 AP 隔离、公司 Wi-Fi 禁止终端互访，改用手机热点或可互访的家庭网络。

## 5. 日常命令

```bash
# 查看容器
docker compose --env-file deploy/local/.env.local.example -f deploy/local/compose.yml ps

# 重跑烟测
bash deploy/local/smoke-test.sh

# 在真实本地 MySQL 上执行目标、食品、饮食、运动、首页、趋势、日历和账号删除主链路
node deploy/local/main-flow-test.mjs

# 停止服务但保留数据
bash deploy/local/stop.sh

# 明确确认后删除本地数据库和头像卷
bash deploy/local/reset-data.sh --confirm
```

存在 `.env.local` 时，上述脚本自动优先使用它。日常迁移由 Flyway 管理，禁止手工改 `flyway_schema_history`。

## 6. 本地完成定义

- 自动化：小程序全部 Node 测试、后端 H2/MySQL 8 测试、静态检查和本地 Compose 烟测通过。
- 数据：Flyway V8、7 个系统分类、48 个系统食品存在。
- 登录：真实 AppID/Secret 下首次登录、再次登录和 401 重登成功。
- 主链路：饮食、运动、趋势、日历、目标、资料和账号删除在同一用户数据上对账。
- 真机：同一局域网的 iOS、Android 均完成核心路径，无 P0/P1。
- 文档：API、架构、测试证据和已知问题与代码同步。

## 7. 云端阶段何时开始

只有用户确认项目功能、UI 和本地双端回归全部完成后，才新建独立的云端发布阶段。届时再决定云厂商，并补齐公网主机、数据库备份、HTTPS、微信合法域名、体验版上传、灰度、监控和生产回滚。当前仓库不维护可执行的 staging 部署配置，避免误把未来发布工作当成本地开发阻塞。
