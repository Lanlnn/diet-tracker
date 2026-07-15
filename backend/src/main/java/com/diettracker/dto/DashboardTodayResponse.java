package com.diettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardTodayResponse(
        LocalDate date,
        int goalCalories,
        String goalSource,
        BigDecimal intakeCalories,
        BigDecimal remainingCalories,
        BigDecimal exceededCalories,
        BigDecimal exerciseCalories,
        BigDecimal netCalories,
        NutritionSummary nutrition,
        ExerciseSummary exercise,
        List<MealSummary> meals,
        Advice advice
) {
    public record NutritionSummary(
            NutrientMetric carbs,
            NutrientMetric protein,
            NutrientMetric fat
    ) {}

    public record NutrientMetric(
            BigDecimal amount,
            BigDecimal goal,
            int progressPercent
    ) {}

    public record ExerciseSummary(
            String state,
            int completedCount,
            int durationMinutes,
            BigDecimal caloriesBurned
    ) {}

    public record MealSummary(
            String type,
            String label,
            int itemCount,
            BigDecimal calories,
            List<String> previewItems
    ) {}

    public record Advice(String title, String message) {}
}
