package com.diettracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "admin_user_id") private Long adminUserId;
    @Column(name = "admin_username", nullable = false, length = 64) private String adminUsername;
    @Column(name = "admin_role", nullable = false, length = 30) private String adminRole;
    @Column(nullable = false, length = 80) private String action;
    @Column(name = "object_type", nullable = false, length = 80) private String objectType;
    @Column(name = "object_id", length = 100) private String objectId;
    @Column(name = "request_id", nullable = false, length = 64) private String requestId;
    @Column(length = 200) private String reason;
    @Column(nullable = false, length = 20) private String result;
    @Column(name = "before_summary", columnDefinition = "TEXT") private String beforeSummary;
    @Column(name = "after_summary", columnDefinition = "TEXT") private String afterSummary;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Long getAdminUserId() { return adminUserId; }
    public String getAdminUsername() { return adminUsername; }
    public String getAdminRole() { return adminRole; }
    public String getAction() { return action; }
    public String getObjectType() { return objectType; }
    public String getObjectId() { return objectId; }
    public String getRequestId() { return requestId; }
    public String getReason() { return reason; }
    public String getResult() { return result; }
    public String getBeforeSummary() { return beforeSummary; }
    public String getAfterSummary() { return afterSummary; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setAdminUserId(Long v) { adminUserId = v; }
    public void setAdminUsername(String v) { adminUsername = v; }
    public void setAdminRole(String v) { adminRole = v; }
    public void setAction(String v) { action = v; }
    public void setObjectType(String v) { objectType = v; }
    public void setObjectId(String v) { objectId = v; }
    public void setRequestId(String v) { requestId = v; }
    public void setReason(String v) { reason = v; }
    public void setResult(String v) { result = v; }
    public void setBeforeSummary(String v) { beforeSummary = v; }
    public void setAfterSummary(String v) { afterSummary = v; }
}
