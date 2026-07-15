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
SET food_name_snapshot = f.name,
    base_amount_snapshot = COALESCE(f.base_amount, 100),
    base_unit_snapshot = COALESCE(f.base_unit, 'g'),
    calories_snapshot = f.calories,
    protein_snapshot = f.protein,
    fat_snapshot = f.fat,
    carbs_snapshot = f.carbs
FROM food_item f
WHERE m.food_item_id = f.id;

ALTER TABLE meal_record
    ALTER COLUMN food_name_snapshot SET NOT NULL,
    ALTER COLUMN base_amount_snapshot SET NOT NULL,
    ALTER COLUMN base_unit_snapshot SET NOT NULL,
    ALTER COLUMN calories_snapshot SET NOT NULL,
    ALTER COLUMN protein_snapshot SET NOT NULL,
    ALTER COLUMN fat_snapshot SET NOT NULL,
    ALTER COLUMN carbs_snapshot SET NOT NULL;

CREATE UNIQUE INDEX uk_meal_record_user_request
    ON meal_record(user_id, client_request_id)
    WHERE client_request_id IS NOT NULL;
