DROP DATABASE IF EXISTS diet_tracker_legacy_test;
CREATE DATABASE diet_tracker_legacy_test
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE diet_tracker_legacy_test;

CREATE TABLE food_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(100),
    sort_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT,
    unit VARCHAR(20),
    calories NUMERIC(10,2),
    protein NUMERIC(10,2),
    fat NUMERIC(10,2),
    carbs NUMERIC(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT legacy_fk_food_category
        FOREIGN KEY (category_id) REFERENCES food_category(id)
);

CREATE TABLE meal_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    food_item_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL,
    unit VARCHAR(20),
    record_time TIMESTAMP,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT legacy_fk_meal_food
        FOREIGN KEY (food_item_id) REFERENCES food_item(id)
);

INSERT INTO food_category (name, icon, sort_order)
VALUES ('旧分类', 'legacy', 99), ('主食', 'rice-old', 10);

INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs)
VALUES ('旧鸡胸肉', 1, '份', 248, 46.50, 5.40, 0);

INSERT INTO meal_record (
    meal_date, meal_type, food_item_id, quantity, unit, record_time, note
) VALUES (
    CURRENT_DATE, 'LUNCH', 1, 1, '份', CURRENT_TIMESTAMP, '迁移保留'
);
