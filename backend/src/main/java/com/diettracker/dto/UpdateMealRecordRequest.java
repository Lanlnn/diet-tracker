package com.diettracker.dto;

import com.diettracker.entity.MealRecord.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMealRecordRequest(
        @NotNull(message = "餐次不能为空") MealType mealType,
        @NotNull(message = "数量不能为空")
        @DecimalMin(value = "0.01", message = "数量必须大于 0") BigDecimal quantity,
        @Size(max = 20) String unit,
        @Size(max = 500) String note) {
}
