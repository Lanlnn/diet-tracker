ALTER TABLE meal_record
    ADD COLUMN food_name_snapshot VARCHAR(100),
    ADD COLUMN base_amount_snapshot NUMERIC(10,2),
    ADD COLUMN base_unit_snapshot VARCHAR(20),
    ADD COLUMN calories_snapshot NUMERIC(10,2),
    ADD COLUMN protein_snapshot NUMERIC(10,2),
    ADD COLUMN fat_snapshot NUMERIC(10,2),
    ADD COLUMN carbs_snapshot NUMERIC(10,2),
    ADD COLUMN client_request_id VARCHAR(100);

UPDATE meal_record m
JOIN food_item f ON m.food_item_id = f.id
SET m.food_name_snapshot = f.name,
    m.base_amount_snapshot = COALESCE(f.base_amount, 100),
    m.base_unit_snapshot = COALESCE(f.base_unit, 'g'),
    m.calories_snapshot = f.calories,
    m.protein_snapshot = f.protein,
    m.fat_snapshot = f.fat,
    m.carbs_snapshot = f.carbs;

ALTER TABLE meal_record
    MODIFY COLUMN food_name_snapshot VARCHAR(100) NOT NULL,
    MODIFY COLUMN base_amount_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN base_unit_snapshot VARCHAR(20) NOT NULL,
    MODIFY COLUMN calories_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN protein_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN fat_snapshot NUMERIC(10,2) NOT NULL,
    MODIFY COLUMN carbs_snapshot NUMERIC(10,2) NOT NULL;

CREATE UNIQUE INDEX uk_meal_record_user_request
    ON meal_record(user_id, client_request_id);
