package com.diettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TrendResponse(
        String range,
        LocalDate startDate,
        LocalDate endDate,
        int calorieGoal,
        int recordedDays,
        BigDecimal averageIntake,
        BigDecimal averageExercise,
        BigDecimal averageNetIntake,
        Integer netChangePercent,
        int nutritionAchievementRate,
        String accessibilitySummary,
        List<TrendDay> dailyData,
        List<TrendSummary> summaries
) {
    public record TrendDay(
            LocalDate date,
            BigDecimal intakeCalories,
            BigDecimal exerciseCalories,
            BigDecimal netCalories,
            boolean hasData
    ) {}

    public record TrendSummary(String type, String title, String message) {}
}
