ALTER TABLE food_item ADD COLUMN base_amount NUMERIC(10,2) NOT NULL DEFAULT 100;
ALTER TABLE food_item ADD COLUMN base_unit VARCHAR(20) NOT NULL DEFAULT 'g';
ALTER TABLE food_item ADD COLUMN serving_amount NUMERIC(10,2);
ALTER TABLE food_item ADD COLUMN serving_unit VARCHAR(20);
ALTER TABLE food_item ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'SYSTEM';

-- Existing records predate the 100g contract. Keep their original unit explicit
-- instead of silently treating per-serving values as per-100g values.
UPDATE food_item
SET base_amount = 1,
    base_unit = COALESCE(NULLIF(unit, ''), '份'),
    serving_amount = 1,
    serving_unit = COALESCE(NULLIF(unit, ''), '份'),
    source = CASE WHEN user_id IS NULL THEN 'LEGACY_SYSTEM' ELSE 'LEGACY_CUSTOM' END;

CREATE TABLE food_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    food_item_id BIGINT NOT NULL REFERENCES food_item(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_food_favorite_user_food UNIQUE (user_id, food_item_id)
);

CREATE INDEX idx_food_favorite_user_created ON food_favorite(user_id, created_at DESC);
