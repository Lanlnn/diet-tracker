package com.diettracker.dto;

import com.diettracker.entity.User;
import com.diettracker.entity.UserGoal;

import java.math.BigDecimal;

public record UserProfileResponse(
        String nickname,
        String avatarUrl,
        String goalType,
        Integer dailyCalorieGoal,
        BigDecimal currentWeight,
        BigDecimal targetWeight,
        int streakDays) {

    public static UserProfileResponse from(User user, UserGoal goal, int streakDays) {
        return new UserProfileResponse(
                user.getNickname() == null ? "" : user.getNickname(),
                user.getAvatarUrl() == null ? "" : user.getAvatarUrl(),
                goal.getGoalType(),
                goal.getDailyCalorieGoal(),
                goal.getCurrentWeight(),
                goal.getTargetWeight(),
                streakDays);
    }
}
