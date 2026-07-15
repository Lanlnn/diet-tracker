package com.diettracker.dto;

import com.diettracker.entity.ExerciseRecord;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExerciseRecordRequest(
        @NotNull LocalDate exerciseDate,
        @NotNull ExerciseRecord.ExerciseType exerciseType,
        LocalTime startTime,
        @Min(1) @Max(600) int durationMinutes,
        @NotNull ExerciseRecord.Intensity intensity,
        @DecimalMin(value = "0.1") @Digits(integer = 8, fraction = 2) BigDecimal caloriesBurned,
        ExerciseRecord.Source source,
        @Size(max = 500) String note
) {}
