# 小程序前端目录约定

微信开发者工具应直接打开本目录，而不是仓库上层目录。

```text
miniapp/
├── pages/          # 已注册页面；当前为 4 个 TabBar 页面
├── services/       # 网络请求及按业务划分的 API
├── shared/         # 环境配置、业务常量、纯工具和 WXS
└── assets/         # 图片、图标等静态资源
```

## 代码边界

- 页面负责展示、交互和组织调用，不直接使用 `wx.request`。
- `services/request.js` 统一处理服务地址、Token、401 重试和上传。
- `services/auth.js`、`food.js`、`record.js` 按业务维护接口。
- `shared` 中的工具不依赖页面、接口和 `getApp()`；配置、常量与工具按文件区分。
- 只有多个页面实际复用的视图才新建 `components`；当前没有公共组件，因此不保留空目录。

## 后续分包

训练、用户中心等功能正式开发时再加入分包并注册到 `app.json`：

```text
package-food/pages/       # 食品搜索、自定义食品、餐次详情
package-training/pages/   # 训练记录、训练计划、AI 教练
package-user/pages/       # 个人资料、目标、隐私、关于
```

不要提前在 `app.json` 注册不存在的分包页面，否则会导致构建失败。
