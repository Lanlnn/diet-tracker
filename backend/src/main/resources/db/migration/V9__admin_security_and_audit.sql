CREATE TABLE admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    session_version BIGINT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_admin_user_username UNIQUE (username)
);

CREATE TABLE admin_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    admin_user_id BIGINT NULL,
    admin_username VARCHAR(64) NOT NULL,
    admin_role VARCHAR(30) NOT NULL,
    action VARCHAR(80) NOT NULL,
    object_type VARCHAR(80) NOT NULL,
    object_id VARCHAR(100),
    request_id VARCHAR(64) NOT NULL,
    reason VARCHAR(200),
    result VARCHAR(20) NOT NULL,
    before_summary TEXT,
    after_summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_audit_user FOREIGN KEY (admin_user_id) REFERENCES admin_user(id)
);

CREATE INDEX idx_admin_audit_operator_time ON admin_audit_log(admin_user_id, created_at);
CREATE INDEX idx_admin_audit_object ON admin_audit_log(object_type, object_id);
CREATE INDEX idx_admin_audit_request ON admin_audit_log(request_id);
