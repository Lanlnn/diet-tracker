package com.diettracker.admin.dto;

import com.diettracker.entity.FoodItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomFoodDiagnosticResponse(Long id, String userRef, String name, BigDecimal baseAmount,
        String baseUnit, BigDecimal calories, BigDecimal protein, BigDecimal fat, BigDecimal carbs,
        LocalDateTime updatedAt) {
    public static CustomFoodDiagnosticResponse from(FoodItem food, String userRef) {
        return new CustomFoodDiagnosticResponse(food.getId(), userRef, food.getName(), food.getBaseAmount(),
                food.getBaseUnit(), food.getCalories(), food.getProtein(), food.getFat(), food.getCarbs(), food.getUpdatedAt());
    }
}
