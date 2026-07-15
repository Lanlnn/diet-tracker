# V8 升级与 U8 回滚演练

## 环境

- MySQL 8.0.46 一次性容器
- Java 18.0.2
- 空数据库 `diet_tracker_test`

## 结果

1. 应用从空库执行 V1–V8，Flyway 成功应用 8 个迁移。
2. Hibernate `ddl-auto=validate` 成功。
3. `MySqlMigrationTest` 验证系统食品、`user_goal`、`account_deletion_audit` 和目标回填数量，测试通过且未跳过。
4. 执行 `U8__user_goals_and_deletion_audit.sql` 后，两张 V8 表数量为 0，Flyway 最新成功版本为 7。
5. 再次启动迁移测试，Flyway 从 V7 重新应用 V8，Hibernate 校验和迁移契约再次通过。
6. 另建旧版结构夹具，脚本先生成备份，再归一化为 V5、升级至 V8；用户 0→1、分类 2→8、食品 1→49、餐次 1→1、收藏 0→0，核心表行数均未减少。

演练发现并修复了原 U8 未删除 `flyway_schema_history` V8 记录的问题；否则旧版应用会因已应用但无法解析的迁移而拒绝启动。
