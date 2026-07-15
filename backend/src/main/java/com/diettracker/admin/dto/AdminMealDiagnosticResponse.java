package com.diettracker.admin.dto;

import com.diettracker.entity.MealRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminMealDiagnosticResponse(
        Long recordId,
        LocalDate mealDate,
        MealRecord.MealType mealType,
        String foodName,
        BigDecimal quantity,
        String unit,
        BigDecimal snapshotBaseAmount,
        String snapshotBaseUnit,
        BigDecimal snapshotCalories,
        BigDecimal snapshotProtein,
        BigDecimal snapshotFat,
        BigDecimal snapshotCarbs,
        String idempotencyKey,
        LocalDateTime recordTime,
        LocalDateTime createdAt
) {}
