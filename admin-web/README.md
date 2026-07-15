# 轻养后台管理前端

本目录是独立的 React + Vite 管理端，对应 A1 管理员安全与前端骨架、A2 系统食品后台 MVP。

## 本地启动

先启动后端，再启动管理端：

```bash
npm install
npm run dev
```

开发服务器默认将 `/api` 代理到 `http://127.0.0.1:8080`。如后端端口不同：

```bash
ADMIN_API_TARGET=http://127.0.0.1:19090 npm run dev
```

## 后端安全配置

生产或本地首次创建管理员时，需要设置：

```bash
export ADMIN_JWT_SECRET='至少 32 字符的随机密钥'
export ADMIN_BOOTSTRAP_USERNAME='admin'
export ADMIN_BOOTSTRAP_PASSWORD='至少 12 字符的高强度密码'
export ADMIN_BOOTSTRAP_DISPLAY_NAME='系统管理员'
```

首次启动成功、管理员创建完成后，应移除 `ADMIN_BOOTSTRAP_*` 环境变量。后台管理员与小程序用户使用独立账号表、JWT audience 和会话版本，不能互换令牌。

## 校验

```bash
npm run lint
npm run build
```

冻结契约见 [`../docs/admin/openapi.yaml`](../docs/admin/openapi.yaml)，阶段范围与验收口径见 [`../docs/ADMIN-DEVELOPMENT.md`](../docs/ADMIN-DEVELOPMENT.md)。
