CREATE TABLE user_goal (
    user_id VARCHAR(100) PRIMARY KEY,
    daily_calorie_goal INTEGER NOT NULL DEFAULT 1800,
    carbs_goal NUMERIC(8,2) NOT NULL DEFAULT 225,
    protein_goal NUMERIC(8,2) NOT NULL DEFAULT 90,
    fat_goal NUMERIC(8,2) NOT NULL DEFAULT 60,
    current_weight NUMERIC(6,2),
    target_weight NUMERIC(6,2),
    goal_type VARCHAR(20) NOT NULL DEFAULT 'MAINTAIN',
    ai_coach_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    customized BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_goal_user FOREIGN KEY (user_id) REFERENCES users(openid) ON DELETE CASCADE,
    CONSTRAINT chk_user_goal_type CHECK (goal_type IN ('LOSE_FAT', 'MAINTAIN', 'BUILD_MUSCLE')),
    CONSTRAINT chk_user_goal_calories CHECK (daily_calorie_goal BETWEEN 1000 AND 5000),
    CONSTRAINT chk_user_goal_carbs CHECK (carbs_goal BETWEEN 0 AND 1000),
    CONSTRAINT chk_user_goal_protein CHECK (protein_goal BETWEEN 0 AND 500),
    CONSTRAINT chk_user_goal_fat CHECK (fat_goal BETWEEN 0 AND 300),
    CONSTRAINT chk_user_goal_current_weight CHECK (current_weight IS NULL OR current_weight BETWEEN 20 AND 500),
    CONSTRAINT chk_user_goal_target_weight CHECK (target_weight IS NULL OR target_weight BETWEEN 20 AND 500)
);

INSERT INTO user_goal (
    user_id, daily_calorie_goal, carbs_goal, protein_goal, fat_goal,
    current_weight, target_weight, goal_type, ai_coach_enabled, customized
)
SELECT openid,
       COALESCE(daily_calorie_goal, 1800),
       ROUND(COALESCE(daily_calorie_goal, 1800) * 0.50 / 4, 2),
       ROUND(COALESCE(daily_calorie_goal, 1800) * 0.20 / 4, 2),
       ROUND(COALESCE(daily_calorie_goal, 1800) * 0.30 / 9, 2),
       current_weight,
       target_weight,
       COALESCE(goal_type, 'MAINTAIN'),
       TRUE,
       (daily_calorie_goal IS NOT NULL OR current_weight IS NOT NULL OR target_weight IS NOT NULL OR goal_type IS NOT NULL)
FROM users;

CREATE TABLE account_deletion_audit (
    event_id VARCHAR(36) PRIMARY KEY,
    user_hash VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    result VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_account_deletion_audit_created_at ON account_deletion_audit(created_at);
