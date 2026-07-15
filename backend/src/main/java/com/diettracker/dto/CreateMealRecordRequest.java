package com.diettracker.dto;

import com.diettracker.entity.MealRecord.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateMealRecordRequest(
        @NotNull(message = "日期不能为空") LocalDate mealDate,
        @NotNull(message = "餐次不能为空") MealType mealType,
        @NotNull(message = "食品不能为空") Long foodItemId,
        @NotNull(message = "数量不能为空") @DecimalMin(value = "0.01", message = "数量必须大于 0") BigDecimal quantity,
        @Size(max = 20) String unit,
        LocalDateTime recordTime,
        @Size(max = 500) String note) {
}
