-- One-time, idempotent normalization for the pre-Flyway MySQL schema.
-- The caller must set @legacy_user_id before sourcing this file.

DROP PROCEDURE IF EXISTS assert_table_exists;
DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS add_constraint_if_missing;
DROP PROCEDURE IF EXISTS assert_no_unowned_meals;

DELIMITER //
CREATE PROCEDURE assert_table_exists(IN target_table VARCHAR(64))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = target_table
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Legacy database is missing a required core table';
    END IF;
END//

CREATE PROCEDURE add_column_if_missing(
    IN target_table VARCHAR(64), IN target_column VARCHAR(64), IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = target_table AND column_name = target_column
    ) THEN
        SET @ddl = ddl_statement;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

CREATE PROCEDURE add_index_if_missing(
    IN target_table VARCHAR(64), IN target_index VARCHAR(64), IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = target_table AND index_name = target_index
    ) THEN
        SET @ddl = ddl_statement;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

CREATE PROCEDURE add_constraint_if_missing(
    IN target_table VARCHAR(64), IN target_constraint VARCHAR(64), IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = DATABASE() AND table_name = target_table AND constraint_name = target_constraint
    ) THEN
        SET @ddl = ddl_statement;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

CREATE PROCEDURE assert_no_unowned_meals()
BEGIN
    IF EXISTS (SELECT 1 FROM meal_record WHERE user_id IS NULL LIMIT 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Legacy meal records need LEGACY_USER_ID before migration';
    END IF;
END//
DELIMITER ;

CALL assert_table_exists('food_category');
CALL assert_table_exists('food_item');
CALL assert_table_exists('meal_record');

CREATE TABLE IF NOT EXISTS users (
    openid VARCHAR(100) PRIMARY KEY,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SET @had_food_basis = EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'food_item' AND column_name = 'base_amount'
);

CALL add_column_if_missing('users', 'goal_type',
    'ALTER TABLE users ADD COLUMN goal_type VARCHAR(20)');
CALL add_column_if_missing('users', 'daily_calorie_goal',
    'ALTER TABLE users ADD COLUMN daily_calorie_goal INTEGER');
CALL add_column_if_missing('users', 'current_weight',
    'ALTER TABLE users ADD COLUMN current_weight NUMERIC(6,2)');
CALL add_column_if_missing('users', 'target_weight',
    'ALTER TABLE users ADD COLUMN target_weight NUMERIC(6,2)');

CALL add_column_if_missing('food_item', 'user_id',
    'ALTER TABLE food_item ADD COLUMN user_id VARCHAR(100)');
CALL add_column_if_missing('food_item', 'base_amount',
    'ALTER TABLE food_item ADD COLUMN base_amount NUMERIC(10,2) NULL');
CALL add_column_if_missing('food_item', 'base_unit',
    'ALTER TABLE food_item ADD COLUMN base_unit VARCHAR(20) NULL');
CALL add_column_if_missing('food_item', 'serving_amount',
    'ALTER TABLE food_item ADD COLUMN serving_amount NUMERIC(10,2)');
CALL add_column_if_missing('food_item', 'serving_unit',
    'ALTER TABLE food_item ADD COLUMN serving_unit VARCHAR(20)');
CALL add_column_if_missing('food_item', 'source',
    'ALTER TABLE food_item ADD COLUMN source VARCHAR(30) NULL');

UPDATE food_item
SET calories = COALESCE(calories, 0),
    protein = COALESCE(protein, 0),
    fat = COALESCE(fat, 0),
    carbs = COALESCE(carbs, 0);

UPDATE food_item
SET base_amount = 1,
    base_unit = COALESCE(NULLIF(unit, ''), '份'),
    serving_amount = 1,
    serving_unit = COALESCE(NULLIF(unit, ''), '份'),
    source = CASE WHEN user_id IS NULL THEN 'LEGACY_SYSTEM' ELSE 'LEGACY_CUSTOM' END
WHERE @had_food_basis = 0;

UPDATE food_item
SET base_amount = COALESCE(base_amount, 100),
    base_unit = COALESCE(NULLIF(base_unit, ''), 'g'),
    source = COALESCE(NULLIF(source, ''), CASE WHEN user_id IS NULL THEN 'SYSTEM' ELSE 'CUSTOM' END);

ALTER TABLE food_item
    MODIFY COLUMN base_amount NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN base_unit VARCHAR(20) NOT NULL,
    MODIFY COLUMN source VARCHAR(30) NOT NULL;

CALL add_column_if_missing('meal_record', 'user_id',
    'ALTER TABLE meal_record ADD COLUMN user_id VARCHAR(100)');
CALL add_column_if_missing('meal_record', 'food_name_snapshot',
    'ALTER TABLE meal_record ADD COLUMN food_name_snapshot VARCHAR(100)');
CALL add_column_if_missing('meal_record', 'base_amount_snapshot',
    'ALTER TABLE meal_record ADD COLUMN base_amount_snapshot NUMERIC(10,2)');
CALL add_column_if_missing('meal_record', 'base_unit_snapshot',
    'ALTER TABLE meal_record ADD COLUMN base_unit_snapshot VARCHAR(20)');
CALL add_column_if_missing('meal_record', 'calories_snapshot',
    'ALTER TABLE meal_record ADD COLUMN calories_snapshot NUMERIC(10,2)');
CALL add_column_if_missing('meal_record', 'protein_snapshot',
    'ALTER TABLE meal_record ADD COLUMN protein_snapshot NUMERIC(10,2)');
CALL add_column_if_missing('meal_record', 'fat_snapshot',
    'ALTER TABLE meal_record ADD COLUMN fat_snapshot NUMERIC(10,2)');
CALL add_column_if_missing('meal_record', 'carbs_snapshot',
    'ALTER TABLE meal_record ADD COLUMN carbs_snapshot NUMERIC(10,2)');
CALL add_column_if_missing('meal_record', 'client_request_id',
    'ALTER TABLE meal_record ADD COLUMN client_request_id VARCHAR(100)');

INSERT INTO users (openid, nickname, created_at, updated_at)
SELECT @legacy_user_id, '历史数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE COALESCE(@legacy_user_id, '') <> ''
ON DUPLICATE KEY UPDATE openid = VALUES(openid);

UPDATE meal_record
SET user_id = @legacy_user_id
WHERE user_id IS NULL AND COALESCE(@legacy_user_id, '') <> '';

CALL assert_no_unowned_meals();

UPDATE meal_record m
JOIN food_item f ON m.food_item_id = f.id
SET m.food_name_snapshot = COALESCE(m.food_name_snapshot, f.name),
    m.base_amount_snapshot = COALESCE(m.base_amount_snapshot, f.base_amount, 100),
    m.base_unit_snapshot = COALESCE(m.base_unit_snapshot, f.base_unit, 'g'),
    m.calories_snapshot = COALESCE(m.calories_snapshot, f.calories, 0),
    m.protein_snapshot = COALESCE(m.protein_snapshot, f.protein, 0),
    m.fat_snapshot = COALESCE(m.fat_snapshot, f.fat, 0),
    m.carbs_snapshot = COALESCE(m.carbs_snapshot, f.carbs, 0);

ALTER TABLE meal_record
    MODIFY COLUMN user_id VARCHAR(100) NOT NULL,
    MODIFY COLUMN food_name_snapshot VARCHAR(100) NOT NULL,
    MODIFY COLUMN base_amount_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN base_unit_snapshot VARCHAR(20) NOT NULL,
    MODIFY COLUMN calories_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN protein_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN fat_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN carbs_snapshot NUMERIC(10,2) NOT NULL;

-- Preserve legacy category IDs and names. Only append missing standard categories.
INSERT INTO food_category (name, icon, sort_order, created_at, updated_at)
SELECT seed.name, seed.icon, seed.sort_order, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '主食' AS name, 'rice' AS icon, 1 AS sort_order UNION ALL
    SELECT '肉类', 'meat', 2 UNION ALL
    SELECT '蔬菜', 'vegetable', 3 UNION ALL
    SELECT '水果', 'fruit', 4 UNION ALL
    SELECT '饮品', 'drink', 5 UNION ALL
    SELECT '零食', 'snack', 6 UNION ALL
    SELECT '其他', 'other', 7
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM food_category existing WHERE existing.name = seed.name
);

CREATE TABLE IF NOT EXISTS food_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    food_item_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CALL add_index_if_missing('meal_record', 'idx_meal_record_user_date',
    'CREATE INDEX idx_meal_record_user_date ON meal_record(user_id, meal_date)');
CALL add_index_if_missing('meal_record', 'idx_meal_record_user_time',
    'CREATE INDEX idx_meal_record_user_time ON meal_record(user_id, record_time)');
CALL add_index_if_missing('meal_record', 'uk_meal_record_user_request',
    'CREATE UNIQUE INDEX uk_meal_record_user_request ON meal_record(user_id, client_request_id)');
CALL add_index_if_missing('food_item', 'idx_food_item_user_category',
    'CREATE INDEX idx_food_item_user_category ON food_item(user_id, category_id)');
CALL add_index_if_missing('food_item', 'idx_food_item_name',
    'CREATE INDEX idx_food_item_name ON food_item(name)');
CALL add_index_if_missing('food_favorite', 'uk_food_favorite_user_food',
    'CREATE UNIQUE INDEX uk_food_favorite_user_food ON food_favorite(user_id, food_item_id)');
CALL add_index_if_missing('food_favorite', 'idx_food_favorite_user_created',
    'CREATE INDEX idx_food_favorite_user_created ON food_favorite(user_id, created_at DESC)');

CALL add_constraint_if_missing('food_item', 'fk_food_item_category',
    'ALTER TABLE food_item ADD CONSTRAINT fk_food_item_category FOREIGN KEY (category_id) REFERENCES food_category(id)');
CALL add_constraint_if_missing('meal_record', 'fk_meal_record_food',
    'ALTER TABLE meal_record ADD CONSTRAINT fk_meal_record_food FOREIGN KEY (food_item_id) REFERENCES food_item(id)');
CALL add_constraint_if_missing('food_favorite', 'fk_food_favorite_food',
    'ALTER TABLE food_favorite ADD CONSTRAINT fk_food_favorite_food FOREIGN KEY (food_item_id) REFERENCES food_item(id) ON DELETE CASCADE');
CALL add_constraint_if_missing('users', 'chk_users_goal_type',
    'ALTER TABLE users ADD CONSTRAINT chk_users_goal_type CHECK (goal_type IS NULL OR goal_type IN (''LOSE_FAT'', ''MAINTAIN'', ''BUILD_MUSCLE''))');
CALL add_constraint_if_missing('users', 'chk_users_calorie_goal',
    'ALTER TABLE users ADD CONSTRAINT chk_users_calorie_goal CHECK (daily_calorie_goal IS NULL OR daily_calorie_goal BETWEEN 1000 AND 5000)');
CALL add_constraint_if_missing('users', 'chk_users_current_weight',
    'ALTER TABLE users ADD CONSTRAINT chk_users_current_weight CHECK (current_weight IS NULL OR current_weight BETWEEN 20 AND 500)');
CALL add_constraint_if_missing('users', 'chk_users_target_weight',
    'ALTER TABLE users ADD CONSTRAINT chk_users_target_weight CHECK (target_weight IS NULL OR target_weight BETWEEN 20 AND 500)');

DROP PROCEDURE assert_no_unowned_meals;
DROP PROCEDURE add_constraint_if_missing;
DROP PROCEDURE add_index_if_missing;
DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE assert_table_exists;
