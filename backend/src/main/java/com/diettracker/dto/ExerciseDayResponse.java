package com.diettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExerciseDayResponse(
        LocalDate date,
        BigDecimal totalCalories,
        int totalDurationMinutes,
        List<ExerciseResponse> records,
        WeeklyCompletion weeklyCompletion
) {
    public record WeeklyCompletion(LocalDate startDate, LocalDate endDate, int completedDays, int targetDays) {}
}
