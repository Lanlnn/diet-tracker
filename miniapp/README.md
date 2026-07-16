# 小程序前端目录约定

微信开发者工具应直接打开本目录，而不是仓库上层目录。

当前 develop/trial 标识均连接同一局域网的本地后端，默认地址为 `http://192.168.3.25:8080/api`。换网络后先更新 `.env.local` 的 `LOCAL_LAN_IP` 并重启后端，再在 Console 执行 `wx.setStorageSync('apiBaseUrl', 'http://新IP:8080/api')` 并重新编译。正式 release 不读取该覆盖。完整步骤见 [`../docs/LOCAL-DEVELOPMENT.md`](../docs/LOCAL-DEVELOPMENT.md)。

```text
miniapp/
├── pages/          # 今日、趋势、记录、运动、我的五个 Tab 页面
├── components/     # 卡片、按钮、分段、状态、进度、列表行
├── packageTools/   # M1 组件预览分包
├── services/       # 网络请求及按业务划分的 API
├── shared/         # 环境配置、业务常量、纯工具和 WXS
└── assets/         # 图片、图标等静态资源
```

## 代码边界

- 页面负责展示、交互和组织调用，不直接使用 `wx.request`。
- `services/request.js` 统一处理服务地址、Token、401 重试和上传。
- `services/auth.js`、`food.js`、`record.js` 按业务维护接口。
- `shared` 中的工具不依赖页面、接口和 `getApp()`；配置、常量与工具按文件区分。
- 公共组件的点击区域不小于 88rpx，并覆盖加载、空、错误、禁用和长文本状态。
- `我的 → 组件预览` 用于检查 M1 Token 与组件状态。

## 分包约定

页面增加时按业务加入分包并注册到 `app.json`：

```text
package-food/pages/       # 食品搜索、自定义食品、餐次详情
package-training/pages/   # 训练记录、训练计划、AI 教练
package-user/pages/       # 个人资料、目标、隐私、关于
```

不要提前在 `app.json` 注册不存在的分包页面，否则会导致构建失败。
