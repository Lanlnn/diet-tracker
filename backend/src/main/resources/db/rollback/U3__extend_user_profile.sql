ALTER TABLE users DROP CHECK chk_users_target_weight;
ALTER TABLE users DROP CHECK chk_users_current_weight;
ALTER TABLE users DROP CHECK chk_users_calorie_goal;
ALTER TABLE users DROP CHECK chk_users_goal_type;
ALTER TABLE users DROP COLUMN IF EXISTS target_weight;
ALTER TABLE users DROP COLUMN IF EXISTS current_weight;
ALTER TABLE users DROP COLUMN IF EXISTS daily_calorie_goal;
ALTER TABLE users DROP COLUMN IF EXISTS goal_type;
