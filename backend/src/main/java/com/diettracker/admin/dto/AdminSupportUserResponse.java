package com.diettracker.admin.dto;

import com.diettracker.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSupportUserResponse(
        String supportRef,
        String nickname,
        String goalType,
        Integer dailyCalorieGoal,
        BigDecimal currentWeight,
        BigDecimal targetWeight,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminSupportUserResponse from(User user) {
        return new AdminSupportUserResponse(user.getSupportRef(), user.getNickname(), user.getGoalType(),
                user.getDailyCalorieGoal(), user.getCurrentWeight(), user.getTargetWeight(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
