ALTER TABLE users ADD COLUMN goal_type VARCHAR(20);
ALTER TABLE users ADD COLUMN daily_calorie_goal INTEGER;
ALTER TABLE users ADD COLUMN current_weight NUMERIC(6,2);
ALTER TABLE users ADD COLUMN target_weight NUMERIC(6,2);

ALTER TABLE users ADD CONSTRAINT chk_users_goal_type
    CHECK (goal_type IS NULL OR goal_type IN ('LOSE_FAT', 'MAINTAIN', 'BUILD_MUSCLE'));
ALTER TABLE users ADD CONSTRAINT chk_users_calorie_goal
    CHECK (daily_calorie_goal IS NULL OR daily_calorie_goal BETWEEN 1000 AND 5000);
ALTER TABLE users ADD CONSTRAINT chk_users_current_weight
    CHECK (current_weight IS NULL OR current_weight BETWEEN 20 AND 500);
ALTER TABLE users ADD CONSTRAINT chk_users_target_weight
    CHECK (target_weight IS NULL OR target_weight BETWEEN 20 AND 500);
