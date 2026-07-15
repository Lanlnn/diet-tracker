package com.diettracker.dto;

import com.diettracker.entity.User;

import java.math.BigDecimal;

public record UserProfileResponse(
        String nickname,
        String avatarUrl,
        String goalType,
        Integer dailyCalorieGoal,
        BigDecimal currentWeight,
        BigDecimal targetWeight,
        int streakDays) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getNickname() == null ? "" : user.getNickname(),
                user.getAvatarUrl() == null ? "" : user.getAvatarUrl(),
                user.getGoalType() == null ? "" : user.getGoalType(),
                user.getDailyCalorieGoal(),
                user.getCurrentWeight(),
                user.getTargetWeight(),
                0);
    }
}
