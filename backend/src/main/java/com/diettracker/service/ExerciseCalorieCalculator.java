package com.diettracker.service;

import com.diettracker.entity.ExerciseRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ExerciseCalorieCalculator {
    private final Map<ExerciseRecord.ExerciseType, BigDecimal> typeRates;
    private final Map<ExerciseRecord.Intensity, BigDecimal> intensityFactors;

    public ExerciseCalorieCalculator(
            @Value("${app.exercise.kcal-per-minute.walking:4.0}") BigDecimal walking,
            @Value("${app.exercise.kcal-per-minute.running:8.0}") BigDecimal running,
            @Value("${app.exercise.kcal-per-minute.cycling:6.5}") BigDecimal cycling,
            @Value("${app.exercise.kcal-per-minute.strength:6.0}") BigDecimal strength,
            @Value("${app.exercise.kcal-per-minute.yoga:3.0}") BigDecimal yoga,
            @Value("${app.exercise.kcal-per-minute.stretching:2.5}") BigDecimal stretching,
            @Value("${app.exercise.kcal-per-minute.other:4.0}") BigDecimal other,
            @Value("${app.exercise.intensity-factor.low:0.8}") BigDecimal low,
            @Value("${app.exercise.intensity-factor.medium:1.0}") BigDecimal medium,
            @Value("${app.exercise.intensity-factor.high:1.25}") BigDecimal high) {
        typeRates = new EnumMap<>(ExerciseRecord.ExerciseType.class);
        typeRates.put(ExerciseRecord.ExerciseType.walking, walking);
        typeRates.put(ExerciseRecord.ExerciseType.running, running);
        typeRates.put(ExerciseRecord.ExerciseType.cycling, cycling);
        typeRates.put(ExerciseRecord.ExerciseType.strength, strength);
        typeRates.put(ExerciseRecord.ExerciseType.yoga, yoga);
        typeRates.put(ExerciseRecord.ExerciseType.stretching, stretching);
        typeRates.put(ExerciseRecord.ExerciseType.other, other);
        intensityFactors = new EnumMap<>(ExerciseRecord.Intensity.class);
        intensityFactors.put(ExerciseRecord.Intensity.low, low);
        intensityFactors.put(ExerciseRecord.Intensity.medium, medium);
        intensityFactors.put(ExerciseRecord.Intensity.high, high);
    }

    public BigDecimal calculate(ExerciseRecord.ExerciseType type, ExerciseRecord.Intensity intensity, int minutes) {
        return typeRates.get(type).multiply(intensityFactors.get(intensity))
                .multiply(BigDecimal.valueOf(minutes)).setScale(1, RoundingMode.HALF_UP);
    }
}
