package com.diettracker.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AdminFoodPreviewRequest(@Valid @NotNull AdminFoodInput food,
                                      @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal amount) {
}
