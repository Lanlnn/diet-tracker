package com.diettracker.admin.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AdminFoodInput(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.01") BigDecimal baseAmount,
        @NotBlank @Size(max = 20) String baseUnit,
        @DecimalMin("0.01") BigDecimal servingAmount,
        @Size(max = 20) String servingUnit,
        @NotNull @DecimalMin("0") BigDecimal calories,
        @NotNull @DecimalMin("0") BigDecimal protein,
        @NotNull @DecimalMin("0") BigDecimal fat,
        @NotNull @DecimalMin("0") BigDecimal carbs,
        @NotBlank @Pattern(regexp = "SYSTEM|SYSTEM_SEED|SYSTEM_EDITORIAL") String source) {
}
