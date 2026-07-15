package com.diettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_deletion_audit")
public class AccountDeletionAudit {
    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;
    @Column(name = "user_hash", nullable = false, length = 64)
    private String userHash;
    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;
    @Column(nullable = false, length = 20)
    private String result;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public String getEventId() { return eventId; }
    public void setEventId(String value) { eventId = value; }
    public String getUserHash() { return userHash; }
    public void setUserHash(String value) { userHash = value; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String value) { requestId = value; }
    public String getResult() { return result; }
    public void setResult(String value) { result = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
