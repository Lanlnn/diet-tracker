package com.diettracker.entity;

import com.diettracker.admin.AdminRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_user")
public class AdminUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AdminRole role;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "session_version", nullable = false)
    private long sessionVersion;
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public AdminRole getRole() { return role; }
    public void setRole(AdminRole value) { role = value; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public long getSessionVersion() { return sessionVersion; }
    public void setSessionVersion(long value) { sessionVersion = value; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime value) { lastLoginAt = value; }
}
