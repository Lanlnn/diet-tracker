package com.diettracker.dto;

import com.diettracker.entity.UserGoal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserGoalResponse(
        int dailyCalorieGoal,
        BigDecimal carbsGoal,
        BigDecimal proteinGoal,
        BigDecimal fatGoal,
        BigDecimal currentWeight,
        BigDecimal targetWeight,
        String goalType,
        boolean aiCoachEnabled,
        LocalDateTime updatedAt) {
    public static UserGoalResponse from(UserGoal value) {
        return new UserGoalResponse(value.getDailyCalorieGoal(), value.getCarbsGoal(),
                value.getProteinGoal(), value.getFatGoal(), value.getCurrentWeight(),
                value.getTargetWeight(), value.getGoalType(), value.isAiCoachEnabled(), value.getUpdatedAt());
    }
}
