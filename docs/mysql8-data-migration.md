# MySQL 8 历史数据迁移手册

本手册用于把项目早期、尚未由 Flyway 管理的 MySQL 8 数据库安全升级到当前 V5 结构。迁移会保留用户、食品分类、食品、饮食记录和收藏数据，不会清库。

## 1. 两种数据库路径

- 新建空库：应用启动时由 Flyway 正常执行 `V1`–`V5`，不使用本手册脚本。
- 已有旧数据且没有 `flyway_schema_history`：使用 `backend/scripts/migrate-legacy-mysql8.sh`。
- 已有 `flyway_schema_history`：只走正常 Flyway 升级；安全脚本会主动拒绝执行。

迁移脚本仅支持 MySQL 8 或更高版本，并要求旧库至少存在 `food_category`、`food_item`、`meal_record` 三张核心表。

## 2. 迁移如何处理旧数据

- 迁移前使用 `mysqldump --single-transaction` 自动生成完整 SQL 备份。
- 保留原食品分类 ID 和名称，只补充缺少的标准分类。
- 旧食品营养值原本按“份”保存时，转换为 `base_amount=1`、原 `unit`，来源标记为 `LEGACY_SYSTEM` 或 `LEGACY_CUSTOM`，不会误算成每 100g。
- 为旧饮食记录回填食品名称和营养快照，历史统计不受以后食品资料修改影响。
- 没有 `user_id` 的旧饮食记录必须显式映射到 `LEGACY_USER_ID`；脚本不会猜测用户。
- 完成结构转换后，将数据库登记为 Flyway V5，并执行真实 MySQL Flyway 校验和 Hibernate Schema Validation。
- 最后核对核心表迁移前后行数，任何表的行数减少都会报错并保留备份供恢复。

## 3. 执行前准备

先安排维护窗口并停止所有会写入目标数据库的应用实例。确认本机安装了 `mysql`、`mysqldump`、Java 17，并使用具备目标库 DDL、DML、建过程和创建索引权限的迁移账号。

不要从 Git 历史、聊天或旧文档复制数据库密码。应先在数据库平台轮换凭据，再通过当前 shell 或 Secret 管理平台注入。

检查没有归属用户的饮食记录应交给哪个真实 OpenID。若旧库没有用户字段或存在 `NULL user_id`，必须设置 `LEGACY_USER_ID`。

## 4. 执行迁移

在仓库根目录配置环境变量。数据库名需要填写两次且必须完全一致，这是防止误操作其他库的确认机制：

```bash
export MYSQL_MIGRATION_HOST='127.0.0.1'
export MYSQL_MIGRATION_PORT='3306'
export MYSQL_MIGRATION_DATABASE='diet_tracker'
export MYSQL_MIGRATION_USERNAME='migration_user'
export MYSQL_MIGRATION_PASSWORD='从 Secret 管理平台读取的新密码'
export MIGRATION_CONFIRM_DATABASE='diet_tracker'
export LEGACY_USER_ID='旧记录应归属的真实 OpenID'

bash backend/scripts/migrate-legacy-mysql8.sh
```

没有无主饮食记录时可不设置 `LEGACY_USER_ID`。密码也不会作为命令行参数传给 MySQL 客户端。

脚本成功时会输出每张核心表的迁移前后行数和备份位置。备份默认保存在 `backend/var/backups/`，该目录已被 Git 忽略；应立即把备份复制到受控、加密且有保留策略的备份存储。

## 5. 验收

迁移完成后至少确认：

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT source, base_amount, base_unit, COUNT(*)
FROM food_item
GROUP BY source, base_amount, base_unit;

SELECT COUNT(*) AS missing_snapshots
FROM meal_record
WHERE user_id IS NULL
   OR food_name_snapshot IS NULL
   OR calories_snapshot IS NULL;
```

期望 Flyway 存在成功的 V5 baseline，`missing_snapshots` 为 0。随后使用灰度账号逐项核对食品搜索、当日餐次详情、首页热量合计以及历史日期统计。

## 6. 失败恢复

脚本在任何结构写入前生成备份。若迁移失败，保持应用停止写入，先保存完整日志，再删除失败的目标库并从脚本输出的备份恢复到同名库：

```bash
mysql --host="$MYSQL_MIGRATION_HOST" \
  --port="$MYSQL_MIGRATION_PORT" \
  --user="$MYSQL_MIGRATION_USERNAME" \
  --password < backend/var/backups/目标备份.sql
```

恢复后核对核心表行数，再定位失败原因。不要手工删除某几列、约束或 `flyway_schema_history` 后继续运行生产应用。
