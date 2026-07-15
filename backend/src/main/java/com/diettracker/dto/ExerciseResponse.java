package com.diettracker.dto;

import com.diettracker.entity.ExerciseRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExerciseResponse(
        Long id,
        LocalDate exerciseDate,
        ExerciseRecord.ExerciseType exerciseType,
        String typeLabel,
        LocalTime startTime,
        int durationMinutes,
        ExerciseRecord.Intensity intensity,
        String intensityLabel,
        BigDecimal caloriesBurned,
        ExerciseRecord.Source source,
        String note
) {}
