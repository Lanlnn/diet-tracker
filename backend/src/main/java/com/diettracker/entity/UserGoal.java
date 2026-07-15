package com.diettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_goal")
public class UserGoal {
    @Id
    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "daily_calorie_goal", nullable = false)
    private Integer dailyCalorieGoal = 1800;

    @Column(name = "carbs_goal", nullable = false, precision = 8, scale = 2)
    private BigDecimal carbsGoal = new BigDecimal("225");

    @Column(name = "protein_goal", nullable = false, precision = 8, scale = 2)
    private BigDecimal proteinGoal = new BigDecimal("90");

    @Column(name = "fat_goal", nullable = false, precision = 8, scale = 2)
    private BigDecimal fatGoal = new BigDecimal("60");

    @Column(name = "current_weight", precision = 6, scale = 2)
    private BigDecimal currentWeight;

    @Column(name = "target_weight", precision = 6, scale = 2)
    private BigDecimal targetWeight;

    @Column(name = "goal_type", nullable = false, length = 20)
    private String goalType = "MAINTAIN";

    @Column(name = "ai_coach_enabled", nullable = false)
    private boolean aiCoachEnabled = true;

    @Column(nullable = false)
    private boolean customized;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() { updatedAt = LocalDateTime.now(); }

    public String getUserId() { return userId; }
    public void setUserId(String value) { userId = value; }
    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer value) { dailyCalorieGoal = value; }
    public BigDecimal getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(BigDecimal value) { carbsGoal = value; }
    public BigDecimal getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(BigDecimal value) { proteinGoal = value; }
    public BigDecimal getFatGoal() { return fatGoal; }
    public void setFatGoal(BigDecimal value) { fatGoal = value; }
    public BigDecimal getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(BigDecimal value) { currentWeight = value; }
    public BigDecimal getTargetWeight() { return targetWeight; }
    public void setTargetWeight(BigDecimal value) { targetWeight = value; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String value) { goalType = value; }
    public boolean isAiCoachEnabled() { return aiCoachEnabled; }
    public void setAiCoachEnabled(boolean value) { aiCoachEnabled = value; }
    public boolean isCustomized() { return customized; }
    public void setCustomized(boolean value) { customized = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
