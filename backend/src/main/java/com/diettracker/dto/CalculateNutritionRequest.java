package com.diettracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalculateNutritionRequest(
        @NotNull(message = "请输入食用重量")
        @DecimalMin(value = "1", message = "食用重量不能小于 1g")
        @DecimalMax(value = "10000", message = "食用重量不能超过 10000g")
        @Digits(integer = 5, fraction = 1, message = "食用重量最多保留 1 位小数")
        BigDecimal amount) {
}
