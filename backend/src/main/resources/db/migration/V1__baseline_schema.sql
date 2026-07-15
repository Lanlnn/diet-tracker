CREATE TABLE users (
    openid VARCHAR(100) PRIMARY KEY,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(100),
    sort_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT REFERENCES food_category(id),
    unit VARCHAR(20),
    calories NUMERIC(10,2) NOT NULL DEFAULT 0,
    protein NUMERIC(10,2) NOT NULL DEFAULT 0,
    fat NUMERIC(10,2) NOT NULL DEFAULT 0,
    carbs NUMERIC(10,2) NOT NULL DEFAULT 0,
    user_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_record (
    id BIGSERIAL PRIMARY KEY,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    food_item_id BIGINT NOT NULL REFERENCES food_item(id),
    quantity NUMERIC(10,2) NOT NULL,
    unit VARCHAR(20),
    record_time TIMESTAMP,
    user_id VARCHAR(100) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meal_record_user_date ON meal_record(user_id, meal_date);
CREATE INDEX idx_meal_record_user_time ON meal_record(user_id, record_time);
CREATE INDEX idx_food_item_user_category ON food_item(user_id, category_id);
CREATE INDEX idx_food_item_name ON food_item(LOWER(name));
