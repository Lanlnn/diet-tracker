package com.diettracker.dto;

import com.diettracker.entity.FoodItem;

import java.math.BigDecimal;

public record FoodResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal baseAmount,
        String baseUnit,
        BigDecimal servingAmount,
        String servingUnit,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs,
        String source,
        boolean custom,
        boolean favorite) {

    public static FoodResponse from(FoodItem food, boolean favorite) {
        return new FoodResponse(
                food.getId(),
                food.getName(),
                food.getCategory() == null ? null : food.getCategory().getId(),
                food.getCategory() == null ? null : food.getCategory().getName(),
                food.getBaseAmount(),
                food.getBaseUnit(),
                food.getServingAmount(),
                food.getServingUnit(),
                food.getCalories(),
                food.getProtein(),
                food.getFat(),
                food.getCarbs(),
                food.getSource(),
                food.getUserId() != null,
                favorite);
    }
}
