# M2：登录与用户资料基础

**状态：** 已完成
**依赖：** M1
**Git 分支：** `codex/m2-auth-profile`
**阶段标签：** `m2-complete`

## UI 基准

- `08-个人中心.png` 顶部头像、昵称、连续记录天数和目标摘要。
- 本阶段只完成资料基础，不实现完整设置菜单。

## 目标

建立稳定的微信登录、用户会话和资料更新能力，供后续所有用户数据接口复用。

## 后端任务

- 建立 Auth/User Request 与 Response DTO。
- 微信登录响应不得记录 `session_key`、openid 全文或登录 code。
- JWT 增加明确的签发、过期和错误处理。
- Profile 查询与修改通过 `UserService`，Controller 不直接访问 Repository。
- 昵称、头像 URL 和目标字段增加长度与格式校验。
- 设计账号数据删除接口和审计策略，M10 完成 UI。

## 小程序任务

- 页面统一等待登录 Promise。
- 头像选择和昵称输入必须由用户交互触发。
- 保存资料等待接口成功后再更新 UI 和提示。
- 修复资料监听器绑定和注销。
- Token 不输出到控制台；失败状态允许重试。

## 接口

- `POST /auth/login`
- `GET /users/me`
- `PUT /users/me`
- 预留 `DELETE /users/me`

## 测试

- 首次登录、缓存 Token、Token 过期和登录失败。
- 多个页面同时请求时只登录一次。
- 用户只能读取和修改自己的资料。
- 非法昵称、超长头像 URL 和缺失字段返回结构化错误。

## 验收

- [x] 冷启动和热启动会话行为一致。
- [x] 资料保存失败不会显示成功。
- [x] 登录日志和前端日志不包含敏感数据。
- [x] 个人中心顶部可以使用真实用户资料渲染。

## Git 交付

```text
feat(m2): harden authentication and user profile flow
test(m2): cover login refresh and user isolation
docs(m2): update authentication contract
```

推送阶段分支，验收合并后创建 `m2-complete` 标签。

## 删除账号数据预案

- M2 在 API 文档预留 `DELETE /users/me`；M10 完成二次确认 UI 和实际删除。
- 删除事务覆盖用户资料、饮食、运动、自定义食品和头像文件，任一步失败则整体回滚。
- 审计只保存事件 ID、requestId、发生时间、结果和不可逆用户散列，不记录 openid、Token、昵称或业务正文。
- 审计默认保留 180 天；上线前由隐私政策和合规评审确认最终期限。
