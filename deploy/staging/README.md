# Staging 部署

本目录把 E1 体验环境固定为：Java 17 后端容器、独立 MySQL 8.0、仅监听本机的后端端口、Nginx TLS 入口和持久化头像目录。真实 Secret 只进入服务器上的 `deploy/staging/.env`。

## 1. 服务器前置条件

- Linux 主机已安装 Docker Engine、Docker Compose v2、Nginx 和 Certbot。
- `staging.tigercloud.asia` 已解析到该主机；签证书前用两个公网递归 DNS 核对。
- 防火墙只对公网开放 80/443；MySQL 不映射宿主机端口，后端默认仅监听 `127.0.0.1:18080`。如修改 `BACKEND_BIND_PORT`，必须同步 Nginx upstream。
- 微信公众平台已添加 request、uploadFile 合法域名 `https://staging.tigercloud.asia`。

## 2. 冻结配置

```bash
cp deploy/staging/.env.staging.example deploy/staging/.env
chmod 600 deploy/staging/.env
```

替换全部 placeholder。`GIT_SHA` 和 `BACKEND_IMAGE_TAG` 必须等于当前完整提交 SHA，`BUILD_TIME` 使用 UTC ISO-8601。创建持久化目录并让容器内 UID/GID 10001 可写：

```bash
sudo install -d -o 10001 -g 10001 /srv/diet-tracker-staging/uploads/avatars
bash deploy/staging/preflight.sh
```

## 3. 首次部署

先使用 Java 17 构建并运行后端测试，再生成镜像。脚本默认拒绝脏工作树，确保镜像只对应一个可追溯提交：

```bash
bash deploy/staging/build-image.sh
docker image inspect "diet-tracker-backend:$(git rev-parse HEAD)" --format '{{.Id}}'
```

启动独立 MySQL 和后端，等待健康：

```bash
docker compose --env-file deploy/staging/.env -f deploy/staging/compose.yml up -d
docker compose --env-file deploy/staging/.env -f deploy/staging/compose.yml ps
curl --fail --show-error http://127.0.0.1:18080/actuator/health
```

复制 `nginx-staging.conf.example` 到 Nginx 站点目录，先保留 80 端口 ACME 路由签发证书，再启用 443 配置。运行 `nginx -t` 成功后 reload。证书必须包含 `DNS:staging.tigercloud.asia`。

## 4. 验证与数据核对

```bash
bash backend/scripts/check-staging-readiness.sh
set -a; source deploy/staging/.env; set +a
docker compose --env-file deploy/staging/.env -f deploy/staging/compose.yml exec \
  --env MYSQL_PWD="$DB_PASSWORD" mysql \
  mysql --user="$DB_USERNAME" diet_tracker_staging \
  --execute='SELECT version, success FROM flyway_schema_history ORDER BY installed_rank; SELECT COUNT(*) AS category_count FROM food_category; SELECT COUNT(*) AS system_food_count FROM food_item WHERE user_id IS NULL;'
```

执行数据库命令前应从 mode 600 的 `.env` 导出变量，命令输出和终端录屏不得包含密码。随后按 `docs/evidence/e1/02-real-device-acceptance.md` 完成真实登录和主链路。

## 5. 更新与回滚

更新前备份 MySQL 卷对应数据库，并保留上一镜像 SHA。新提交必须重新填写 `GIT_SHA`、`BACKEND_IMAGE_TAG`、`BUILD_TIME` 并通过 preflight。若健康、登录或数据口径异常：

1. 停止体验版扩大范围。
2. 把 `BACKEND_IMAGE_TAG` 改回上一稳定 SHA，仅重建 backend 容器。
3. 若迁移产生不兼容写入，停止写流量，按 `docs/release-and-rollback.md` 的数据库条件恢复备份；不得在已有新目标数据时直接执行 U8。
4. 保存 requestId、镜像 ID、Git SHA 和时间，不保存 Token、openid 或微信 code。
