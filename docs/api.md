# API 接口文档

> 本文记录当前已实现接口。新增或调整接口时，必须与 [`DEVELOPMENT.md`](./DEVELOPMENT.md) 和后端 DTO 同步更新。

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

### 获取食物列表（M3）

```
GET /foods?scope=common&page=0&size=20
GET /foods?scope=recent&page=0&size=20
GET /foods?scope=favorite&page=0&size=20
GET /foods?scope=all&categoryId=1&page=0&size=20
```

| 参数 | 类型 | 说明 |
|------|------|------|
| scope | string | `common` / `recent` / `favorite` / `all`，默认 `common` |
| categoryId | long | 可选分类 ID；传入时按可见食品过滤 |
| page | int | 从 0 开始，默认 0 |
| size | int | 1–50，默认 20 |

**响应**
```json
{
  "items": [
    {
      "id": 1,
      "name": "米饭",
      "categoryId": 1,
      "categoryName": "主食",
      "baseAmount": 100.0,
      "baseUnit": "g",
      "servingAmount": null,
      "servingUnit": null,
      "calories": 116.0,
      "protein": 2.6,
      "fat": 0.3,
      "carbs": 25.0,
      "source": "SYSTEM",
      "custom": false,
      "favorite": false
    }
  ],
  "page": 0,
  "size": 20,
  "total": 1,
  "hasMore": false
}
```

营养字段均对应 `baseAmount + baseUnit`。M3 新基准食品使用每 100g；迁移前无法可靠换算的系统/自定义历史按份数据会分别标记为 `LEGACY_SYSTEM` / `LEGACY_CUSTOM`，并保存为 `baseAmount=1` 和原单位，不得当作每 100g 数据参与计算。

### 搜索食物

```
GET /foods/search?keyword=鸡&page=0&size=20
```

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 必填；清理首尾与连续空白后模糊匹配，最长 50 字符 |
| page / size | int | 与食品列表相同 |

响应为上方分页结构。空关键词返回 `EMPTY_KEYWORD`，用户只能搜索系统食品和自己的自定义食品。

### 添加自定义食物

```
POST /foods
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 食物名称 |
| categoryId | long | - | 分类 ID；不传自动使用“其他” |
| baseAmount | number | - | 营养基准数量，默认 100，必须大于 0 |
| baseUnit | string | - | 营养基准单位，默认 `g` |
| servingAmount | number | - | 可选的每份重量，必须大于 0 |
| servingUnit | string | - | 可选的份量单位 |
| calories | number | - | 基准数量对应热量(kcal)，默认 0 |
| protein / fat / carbs | number | - | 基准数量对应营养素(g)，默认 0 |

```json
// 请求
{ "name": "三明治", "baseAmount": 100, "baseUnit": "g", "calories": 220, "protein": 10 }

// 响应（自动归入「其他」分类）
{
  "id": 17,
  "name": "三明治",
  "categoryId": 7,
  "categoryName": "其他",
  "baseAmount": 100.0,
  "baseUnit": "g",
  "servingAmount": null,
  "servingUnit": null,
  "calories": 220.0,
  "protein": 10.0,
  "fat": 0.0,
  "carbs": 0.0,
  "source": "USER_CUSTOM",
  "custom": true,
  "favorite": false
}
```

### 收藏或取消收藏

```
PUT /foods/{foodId}/favorite
```

```json
{ "favorite": true }
```

响应为单个食品对象。不能收藏其他用户的自定义食品；不可见食品返回 `FOOD_NOT_FOUND`。

### 获取食品详情（M4）

```
GET /foods/{foodId}
```

响应为单个食品对象，字段与食品列表项一致。只能读取系统食品或当前用户创建的自定义食品。

### 计算营养预览（M4）

```
POST /foods/{foodId}/calculate
```

```json
// 请求
{ "amount": 150 }

// 响应
{
  "foodId": 1,
  "foodName": "鸡胸肉",
  "baseAmount": 100,
  "baseUnit": "g",
  "servingAmount": null,
  "servingUnit": null,
  "amount": 150,
  "unit": "g",
  "calories": 248,
  "protein": 46.5,
  "fat": 5.4,
  "carbs": 0.0
}
```

`amount` 范围为 1–10,000g，最多 1 位小数。服务端使用 `BigDecimal` 按食品营养基准重新计算；热量四舍五入为整数，三大营养素保留 1 位小数。空值、越界、多位小数、非克制基准和无效营养字段均返回 `fieldErrors`，客户端计算值不作为服务端结果来源。

---

## 饮食记录

### 添加记录

```
POST /records
```

可选请求头 `X-Idempotency-Key`（最长 100 字符）。同一用户使用相同键重试时返回已创建记录，不重复写入。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealDate | string | ✅ | 日期 `2026-05-15` |
| mealType | string | ✅ | `breakfast` / `lunch` / `dinner` / `snack` |
| foodItemId | int | ✅ | 食物 ID，只能引用系统食品或当前用户食品 |
| quantity | number | ✅ | 食用数量 |
| unit | string | - | 单位 |
| recordTime | string | - | ISO 时间，不传则自动设为当前时间 |
| note | string | - | 备注 |

```json
// 请求
{
  "mealDate": "2026-05-15",
  "mealType": "breakfast",
  "foodItemId": 1,
  "quantity": 150,
  "unit": "g"
}

// 响应
{
  "id": 1,
  "mealDate": "2026-05-15",
  "mealType": "breakfast",
  "quantity": 150,
  "unit": "g",
  "foodNameSnapshot": "米饭",
  "baseAmountSnapshot": 100,
  "baseUnitSnapshot": "g",
  "caloriesSnapshot": 116,
  "proteinSnapshot": 2.6,
  "fatSnapshot": 0.3,
  "carbsSnapshot": 25.9,
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
| mealType | string | 餐次（选填），用于餐次详情 |

响应结构与上方添加记录的响应相同，返回数组。

### 编辑记录

```
PUT /records/{id}
```

```json
{ "mealType": "dinner", "quantity": 180, "unit": "g", "note": "少油" }
```

支持修改数量、餐次、单位和备注；食品与创建时的营养基准快照保持不变。记录不存在返回 404，操作其他用户记录返回 403。

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

记录不存在返回 404，操作其他用户记录返回 403。日/周统计与餐次聚合均只读取记录中的营养快照，不受食品库后续修改影响。

---

## 今日首页聚合

```
GET /dashboard/today?date=2026-07-15
```

首页只请求该接口，服务端在同一只读事务内聚合用户目标和当日饮食快照。未设置热量目标时使用 1,800 千卡运营默认值，并通过 `goalSource=DEFAULT` 标识。

```json
{
  "date": "2026-07-15",
  "goalCalories": 1800,
  "goalSource": "USER",
  "intakeCalories": 1240,
  "remainingCalories": 560,
  "exceededCalories": 0,
  "exerciseCalories": 0,
  "netCalories": 1240,
  "nutrition": {
    "carbs": { "amount": 142, "goal": 225, "progressPercent": 63 },
    "protein": { "amount": 86, "goal": 90, "progressPercent": 96 },
    "fat": { "amount": 41, "goal": 60, "progressPercent": 68 }
  },
  "exercise": {
    "state": "empty",
    "completedCount": 0,
    "durationMinutes": 0,
    "caloriesBurned": 0
  },
  "meals": [
    { "type": "breakfast", "label": "早餐", "itemCount": 1, "calories": 380, "previewItems": ["水煮蛋"] }
  ],
  "advice": { "title": "保持当前节奏", "message": "继续完整记录今日饮食" }
}
```

- `remainingCalories = max(goalCalories - intakeCalories, 0)`
- `exceededCalories = max(intakeCalories - goalCalories, 0)`
- `netCalories = intakeCalories - exerciseCalories`
- 无运动记录时 `exercise.state=empty`；有记录时为 `success`，并聚合次数、时长和消耗。运动消耗不增加 `remainingCalories`。

---

## 今日运动

### 查询当日运动

```
GET /exercises?date=2026-07-15
```

```json
{
  "date": "2026-07-15",
  "totalCalories": 80,
  "totalDurationMinutes": 20,
  "records": [{
    "id": 7,
    "exerciseDate": "2026-07-15",
    "exerciseType": "walking",
    "typeLabel": "户外快走",
    "startTime": "18:00:00",
    "durationMinutes": 20,
    "intensity": "medium",
    "intensityLabel": "中等强度",
    "caloriesBurned": 80,
    "source": "MANUAL",
    "note": "饭后"
  }],
  "weeklyCompletion": {
    "startDate": "2026-07-13",
    "endDate": "2026-07-19",
    "completedDays": 1,
    "targetDays": 4
  }
}
```

### 新增、修改与删除

```
POST /exercises
PUT /exercises/{id}
DELETE /exercises/{id}
```

新增和修改使用同一请求结构：

```json
{
  "exerciseDate": "2026-07-15",
  "exerciseType": "walking",
  "startTime": "18:00",
  "durationMinutes": 20,
  "intensity": "medium",
  "caloriesBurned": 80,
  "source": "MANUAL",
  "note": "饭后"
}
```

`caloriesBurned` 可不传，服务端按可配置的运动类型系数、强度系数与时长估算；保存时会固化最终确认值。修改或删除其他用户的记录返回 403。

### 规则推荐

```
GET /exercise-recommendations?date=2026-07-15
```

返回 0–2 项基于当日已记录时长的固定规则建议，包含运动类型、强度、时长、预计消耗与推荐原因。V1 不调用大模型，不输出诊断、治疗或效果承诺。

---

## 统计

### 热量与运动趋势

```
GET /stats/trend?range=7d
```

`range` 仅支持 `7d`、`30d`、`90d`，不传时默认 `7d`。服务端使用 `APP_TIME_ZONE`（默认 `Asia/Shanghai`）确定统计截止日期，并返回完整连续日期序列，缺失日期补 0。

```json
{
  "range": "7d",
  "startDate": "2026-07-09",
  "endDate": "2026-07-15",
  "calorieGoal": 1800,
  "recordedDays": 3,
  "averageIntake": 1916,
  "averageExercise": 274,
  "averageNetIntake": 1642,
  "netChangePercent": -6,
  "nutritionAchievementRate": 67,
  "accessibilitySummary": "趋势共 7 天；净摄入最高为……",
  "dailyData": [{
    "date": "2026-07-15",
    "intakeCalories": 1916,
    "exerciseCalories": 274,
    "netCalories": 1642,
    "hasData": true
  }],
  "summaries": [{
    "type": "intake-steady",
    "title": "热量控制稳定",
    "message": "近期净摄入处于目标区间，继续保持完整记录。"
  }]
}
```

- 摄入按 `meal_record` 的营养快照和记录克数聚合，运动按保存后的 `calories_burned` 聚合；`netCalories = intakeCalories - exerciseCalories`。
- 三项日均值以完整区间天数为分母；`netChangePercent` 对比前一等长周期，前一周期净摄入为 0 时返回 `null`。
- 营养达成率是有记录日期中，摄入落在用户目标热量 80%–120% 的日期占比。
- 有数据的日期少于 3 天时只返回一条“继续记录”，达到 3 天后最多返回两条规则化小结。

### 饮食日历月摘要（M9）

```
GET /calendar/summary?month=2026-07
```

`month` 必须为严格 `yyyy-MM` 格式，只允许当前月和向前 11 个月。超出范围返回 `MONTH_OUT_OF_RANGE`，格式错误返回 `INVALID_MONTH`。

```json
{
  "month": "2026-07",
  "goalCalories": 1800,
  "goalSource": "USER",
  "days": [{
    "date": "2026-07-13",
    "intakeCalories": 1240,
    "exerciseCalories": 380,
    "remainingCalories": 560,
    "mealCount": 3,
    "hasRecord": true
  }]
}
```

- `days` 始终包含当月全部日期，没有记录的日期数值补 0。
- `mealCount` 是当天不同餐次类型数；`hasRecord` 在存在饮食或运动记录时为 `true`。
- `remainingCalories = max(goalCalories - intakeCalories, 0)`，运动消耗不增加剩余可摄入。
- 整月饮食和运动各执行一次分组聚合；前端月切换不逐日请求。

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
