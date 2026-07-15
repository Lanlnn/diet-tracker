CREATE TABLE exercise_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    exercise_date DATE NOT NULL,
    exercise_type VARCHAR(30) NOT NULL,
    start_time TIME,
    duration_minutes INTEGER NOT NULL,
    intensity VARCHAR(20) NOT NULL,
    calories_burned NUMERIC(10,2) NOT NULL,
    source VARCHAR(30) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exercise_record_user FOREIGN KEY (user_id) REFERENCES users(openid)
);

CREATE INDEX idx_exercise_record_user_date ON exercise_record(user_id, exercise_date);
CREATE INDEX idx_exercise_record_user_time ON exercise_record(user_id, exercise_date, start_time);
