ALTER TABLE users ADD COLUMN support_ref VARCHAR(36) NULL AFTER openid;

UPDATE users
SET support_ref = CONCAT('usr_', REPLACE(UUID(), '-', ''))
WHERE support_ref IS NULL;

ALTER TABLE users MODIFY COLUMN support_ref VARCHAR(36) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_support_ref UNIQUE (support_ref);
