package com.diettracker.dto;

public record ProfileSummaryResponse(
        long customFoodCount,
        long favoriteFoodCount,
        long exerciseCountThisWeek,
        int streakDays) {
}
