package com.diettracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record UpdateGoalRequest(
        @NotNull @Min(1000) @Max(5000) Integer dailyCalorieGoal,
        @NotNull @DecimalMin("0") @DecimalMax("1000") BigDecimal carbsGoal,
        @NotNull @DecimalMin("0") @DecimalMax("500") BigDecimal proteinGoal,
        @NotNull @DecimalMin("0") @DecimalMax("300") BigDecimal fatGoal,
        @DecimalMin("20") @DecimalMax("500") BigDecimal currentWeight,
        @DecimalMin("20") @DecimalMax("500") BigDecimal targetWeight,
        @NotNull @Pattern(regexp = "LOSE_FAT|MAINTAIN|BUILD_MUSCLE") String goalType,
        boolean aiCoachEnabled) {
}
