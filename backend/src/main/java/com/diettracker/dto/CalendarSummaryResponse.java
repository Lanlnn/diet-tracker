package com.diettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record CalendarSummaryResponse(
        YearMonth month,
        int goalCalories,
        String goalSource,
        List<CalendarDay> days
) {
    public record CalendarDay(
            LocalDate date,
            BigDecimal intakeCalories,
            BigDecimal exerciseCalories,
            BigDecimal remainingCalories,
            int mealCount,
            boolean hasRecord
    ) {}
}
