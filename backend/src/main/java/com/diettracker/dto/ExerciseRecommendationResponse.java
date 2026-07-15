package com.diettracker.dto;

import com.diettracker.entity.ExerciseRecord;

import java.math.BigDecimal;

public record ExerciseRecommendationResponse(
        String id,
        String name,
        ExerciseRecord.ExerciseType exerciseType,
        ExerciseRecord.Intensity intensity,
        String intensityLabel,
        int durationMinutes,
        BigDecimal estimatedCalories,
        String reason
) {}
