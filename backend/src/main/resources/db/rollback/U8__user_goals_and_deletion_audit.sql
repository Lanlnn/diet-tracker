DROP TABLE IF EXISTS account_deletion_audit;
DROP TABLE IF EXISTS user_goal;
DELETE FROM flyway_schema_history WHERE version = '8';
