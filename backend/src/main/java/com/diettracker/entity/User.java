 package com.diettracker.entity;
 
 import jakarta.persistence.*;
 import java.time.LocalDateTime;
 
 @Entity
 @Table(name = "users")
 public class User {
 
     @Id
     @Column(length = 100)
     private String openid;
 
     @Column(length = 100)
     private String nickname;
 
     @Column(name = "avatar_url", length = 500)
     private String avatarUrl;
 
     @Column(name = "created_at", updatable = false)
     private LocalDateTime createdAt;
 
     @Column(name = "updated_at")
     private LocalDateTime updatedAt;
 
     @PrePersist
     protected void onCreate() {
         createdAt = LocalDateTime.now();
         updatedAt = LocalDateTime.now();
     }
 
     @PreUpdate
     protected void onUpdate() {
         updatedAt = LocalDateTime.now();
     }
 
     public String getOpenid() { return openid; }
     public void setOpenid(String openid) { this.openid = openid; }
 
     public String getNickname() { return nickname; }
     public void setNickname(String nickname) { this.nickname = nickname; }
 
     public String getAvatarUrl() { return avatarUrl; }
     public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
 
     public LocalDateTime getCreatedAt() { return createdAt; }
     public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
 
     public LocalDateTime getUpdatedAt() { return updatedAt; }
     public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
 }
