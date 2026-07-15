# API 接口文档

> 本文记录当前原型接口，不代表优化版最终契约。优化版新增或调整接口时，必须与 [`DEVELOPMENT.md`](./DEVELOPMENT.md) 和后端 DTO 同步更新。

> 基础路径：`http://localhost:8080/api`
> 请求头：`Content-Type: application/json`
> 除登录接口外，业务接口请求头需要：`Authorization: Bearer <token>`

---

## 登录与个人资料（M2）

### 微信登录

```
POST /auth/login
```

```json
// 请求
{ "code": "wx.login 返回的一次性 code" }

// 响应
{
  "token": "<jwt>",
  "expiresIn": 604800,
  "user": {
    "nickname": "林晓",
    "avatarUrl": "https://img.example/avatar.png",
    "goalType": "LOSE_FAT",
    "dailyCalorieGoal": 1800,
    "currentWeight": 62.4,
    "targetWeight": 58.0,
    "streakDays": 0
  }
}
```

登录响应不返回 `openid` 或 `session_key`。`expiresIn` 单位为秒。

### 查询当前用户资料

```
GET /users/me
```

响应为登录响应中的 `user` 对象。用户身份只从 JWT 获取，不接受客户端传入用户 ID。

### 修改当前用户资料

```
PUT /users/me
```

```json
{
  "nickname": "林晓",
  "avatarUrl": "https://img.example/avatar.png",
  "goalType": "LOSE_FAT",
  "dailyCalorieGoal": 1800,
  "currentWeight": 62.4,
  "targetWeight": 58.0
}
```

| 字段 | 规则 |
|------|------|
| nickname | 必填，去除首尾空格后 1–40 字符 |
| avatarUrl | 可空，最长 500 字符，仅 HTTP/HTTPS URL |
| goalType | 可空；`LOSE_FAT` / `MAINTAIN` / `BUILD_MUSCLE` |
| dailyCalorieGoal | 可空；1,000–5,000 千卡 |
| currentWeight / targetWeight | 可空；20–500 kg |

### 删除账号数据（M10 预留）

`DELETE /users/me` 的 UI 和实际删除逻辑在 M10 实现。接口必须要求有效 JWT 和二次确认；服务端事务内删除用户资料、饮食、运动、自定义食品和上传文件。审计日志只保存事件 ID、requestId、时间、结果和不可逆用户散列，不保存 openid、Token、昵称或业务内容，默认保留 180 天。删除失败必须整体回滚并返回结构化错误。

---

## 食物分类

### 获取所有分类

```
GET /foods/categories
```

**响应**
```json
[
  {
    "id": 1,
    "name": "主食",
    "icon": "rice",
    "sortOrder": 1,
    "createdAt": "2026-05-15T09:37:16",
    "updatedAt": "2026-05-15T09:37:16"
  }
]
```

---

## 食物

### 获取食物列表

```
GET /foods
GET /foods?categoryId=1
```

| 参数 | 类型 | 说明 |
|------|------|------|
| categoryId | int | 分类ID，不传返回全部 |

**响应**
```json
[
  {
    "id": 1,
    "name": "米饭",
    "category": {
      "id": 1, "name": "主食", "icon": "rice",
      "sortOrder": 1, "createdAt": "...", "updatedAt": "..."
    },
    "unit": "碗",
    "calories": 232.0,
    "protein": 2.6,
    "fat": 0.3,
    "carbs": 50.0,
    "createdAt": "2026-05-15T09:37:16",
    "updatedAt": "2026-05-15T09:37:16"
  }
]
```

### 搜索食物

```
GET /foods/search?keyword=鸡
```

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 模糊匹配食物名称 |

响应结构与上方食物列表相同。

### 添加自定义食物

```
POST /foods
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 食物名称 |
| unit | string | - | 单位，默认 `份` |
| calories | number | - | 热量(kcal)，默认 0 |
| protein | number | - | 蛋白质(g)，默认 0 |
| fat | number | - | 脂肪(g)，默认 0 |
| carbs | number | - | 碳水(g)，默认 0 |

```json
// 请求
{ "name": "三明治", "unit": "个", "calories": 350, "protein": 15 }

// 响应（自动归入「其他」分类）
{
  "id": 17,
  "name": "三明治",
  "category": { "id": 7, "name": "其他", "icon": "other", "sortOrder": 7 },
  "unit": "个",
  "calories": 350.0,
  "protein": 15.0,
  "fat": 0.0,
  "carbs": 0.0,
  "createdAt": "2026-05-15T13:40:32",
  "updatedAt": "2026-05-15T13:40:32"
}
```

---

## 饮食记录

### 添加记录

```
POST /records
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealDate | string | ✅ | 日期 `2026-05-15` |
| mealType | string | ✅ | `breakfast` / `lunch` / `dinner` / `snack` |
| foodItem.id | int | ✅ | 食物 ID |
| quantity | number | ✅ | 份数 |
| unit | string | - | 单位 |
| recordTime | string | - | ISO 时间，不传则自动设为当前时间 |
| note | string | - | 备注 |

```json
// 请求
{
  "mealDate": "2026-05-15",
  "mealType": "breakfast",
  "foodItem": { "id": 1 },
  "quantity": 1.0,
  "unit": "碗"
}

// 响应
{
  "id": 1,
  "mealDate": "2026-05-15",
  "mealType": "breakfast",
  "foodItem": {
    "id": 1, "name": "米饭",
    "category": { "id": 1, "name": "主食", "icon": "rice", "sortOrder": 1 },
    "unit": "碗",
    "calories": 232.0, "protein": 2.6, "fat": 0.3, "carbs": 50.0,
    "createdAt": "2026-05-15T09:37:16",
    "updatedAt": "2026-05-15T09:37:16"
  },
  "quantity": 1.0,
  "unit": "碗",
  "recordTime": "2026-05-15T08:00:00",
  "note": null,
  "createdAt": "2026-05-15T09:39:14",
  "updatedAt": "2026-05-15T09:39:14"
}
```

### 按日期查询

```
GET /records?date=2026-05-15
```

| 参数 | 类型 | 说明 |
|------|------|------|
| date | string | 日期（必填） |

响应结构与上方添加记录的响应相同，返回数组。

### 按日期范围查询

```
GET /records/range?start=2026-05-01&end=2026-05-15
```

| 参数 | 类型 | 说明 |
|------|------|------|
| start | string | 起始日期（必填） |
| end | string | 结束日期（必填） |

### 删除记录

```
DELETE /records/{id}
```

**成功返回 204，无响应体。**

---

## 统计

### 日统计

```
GET /records/stats/daily?date=2026-05-15
```

```json
{
  "date": "2026-05-15",
  "totalCalories": 397.0,
  "totalProtein": 33.6,
  "totalFat": 3.9,
  "totalCarbs": 50.0,
  "recordCount": 2
}
```

### 周统计

```
GET /records/stats/weekly?date=2026-05-15
```

自动计算所在周（周一到周日）。

```json
{
  "startDate": "2026-05-11",
  "endDate": "2026-05-17",
  "dailyData": [
    { "date": "2026-05-11", "calories": 0 },
    { "date": "2026-05-15", "calories": 397.0 }
  ]
}
```

---

## 附录

### 餐次枚举

| 值 | 说明 |
|------|------|
| breakfast | 早餐 |
| lunch | 午餐 |
| dinner | 晚餐 |
| snack | 加餐 |

### 预置食物分类

| ID | 名称 | 说明 |
|----|------|------|
| 1 | 主食 | 米饭、面条、馒头等 |
| 2 | 肉类 | 鸡胸肉、牛肉、猪肉、鸡蛋等 |
| 3 | 蔬菜 | 西兰花、菠菜、番茄等 |
| 4 | 水果 | 苹果、香蕉等 |
| 5 | 饮品 | 牛奶、咖啡等 |
| 6 | 零食 | 薯片、巧克力等 |
| 7 | 其他 | 自定义新食物默认归入此类 |

### 公共响应字段

所有返回的实体都包含：

| 字段 | 格式 | 说明 |
|------|------|------|
| id | int | 主键 |
| createdAt | string | ISO 时间，自动生成 |
| updatedAt | string | ISO 时间，自动更新 |

### 错误响应

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不合法",
  "requestId": "0e431e7f-...",
  "fieldErrors": { "nickname": "昵称不能为空" }
}
```

认证错误使用 `AUTH_REQUIRED`、`TOKEN_EXPIRED` 或 `TOKEN_INVALID`；前端收到 401 后只进行一次互斥重新登录和重试。

### 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 204 | 删除成功，无响应体 |
| 400 | 请求参数错误（JSON 格式错误、缺少必填字段） |
| 404 | 资源不存在 |
| 502 | 微信登录服务暂时不可用 |
| 500 | 服务器内部错误 |
