package com.diettracker.dto;

import java.math.BigDecimal;

public record NutritionCalculationResponse(
        Long foodId,
        String foodName,
        BigDecimal baseAmount,
        String baseUnit,
        BigDecimal servingAmount,
        String servingUnit,
        BigDecimal amount,
        String unit,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs) {
}
