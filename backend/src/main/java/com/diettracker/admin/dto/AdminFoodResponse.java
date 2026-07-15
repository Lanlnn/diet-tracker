package com.diettracker.admin.dto;

import com.diettracker.entity.FoodItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminFoodResponse(Long id, String name, Long categoryId, String categoryName,
        BigDecimal baseAmount, String baseUnit, BigDecimal servingAmount, String servingUnit,
        BigDecimal calories, BigDecimal protein, BigDecimal fat, BigDecimal carbs,
        String source, LocalDateTime updatedAt, String lastOperator) {
    public static AdminFoodResponse from(FoodItem food) {
        return new AdminFoodResponse(food.getId(), food.getName(), food.getCategory().getId(), food.getCategory().getName(),
                food.getBaseAmount(), food.getBaseUnit(), food.getServingAmount(), food.getServingUnit(),
                food.getCalories(), food.getProtein(), food.getFat(), food.getCarbs(), food.getSource(),
                food.getUpdatedAt(), null);
    }
}
