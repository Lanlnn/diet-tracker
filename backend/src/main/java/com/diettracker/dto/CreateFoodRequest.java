package com.diettracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateFoodRequest(
        @NotBlank(message = "食品名称不能为空") @Size(max = 100) String name,
        Long categoryId,
        @Size(max = 20) String unit,
        @DecimalMin(value = "0.01", message = "营养基准必须大于 0") BigDecimal baseAmount,
        @Size(max = 20) String baseUnit,
        @DecimalMin(value = "0.01", message = "每份重量必须大于 0") BigDecimal servingAmount,
        @Size(max = 20) String servingUnit,
        @DecimalMin(value = "0", message = "热量不能小于 0") BigDecimal calories,
        @DecimalMin(value = "0", message = "蛋白质不能小于 0") BigDecimal protein,
        @DecimalMin(value = "0", message = "脂肪不能小于 0") BigDecimal fat,
        @DecimalMin(value = "0", message = "碳水不能小于 0") BigDecimal carbs) {
}
