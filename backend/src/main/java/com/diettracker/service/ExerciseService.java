package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.ExerciseDayResponse;
import com.diettracker.dto.ExerciseRecommendationResponse;
import com.diettracker.dto.ExerciseRecordRequest;
import com.diettracker.dto.ExerciseResponse;
import com.diettracker.entity.ExerciseRecord;
import com.diettracker.repository.ExerciseRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExerciseService {
    private static final int WEEKLY_TARGET_DAYS = 4;

    private final ExerciseRecordRepository repository;
    private final ExerciseCalorieCalculator calculator;

    public ExerciseService(ExerciseRecordRepository repository, ExerciseCalorieCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public ExerciseDayResponse getDay(LocalDate date, String userId) {
        List<ExerciseRecord> records = repository
                .findByUserIdAndExerciseDateOrderByStartTimeAscIdAsc(userId, date);
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = date.with(DayOfWeek.SUNDAY);
        long completedDays = repository
                .findByUserIdAndExerciseDateBetweenOrderByExerciseDateAscStartTimeAsc(userId, weekStart, weekEnd)
                .stream().map(ExerciseRecord::getExerciseDate).distinct().count();
        return new ExerciseDayResponse(
                date,
                display(records.stream().map(ExerciseRecord::getCaloriesBurned)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)),
                records.stream().mapToInt(ExerciseRecord::getDurationMinutes).sum(),
                records.stream().map(this::response).toList(),
                new ExerciseDayResponse.WeeklyCompletion(
                        weekStart, weekEnd, (int) completedDays, WEEKLY_TARGET_DAYS)
        );
    }

    @Transactional
    public ExerciseResponse create(ExerciseRecordRequest request, String userId) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        apply(record, request);
        return response(repository.save(record));
    }

    @Transactional
    public ExerciseResponse update(Long id, ExerciseRecordRequest request, String userId) {
        ExerciseRecord record = ownedRecord(id, userId);
        apply(record, request);
        return response(repository.save(record));
    }

    @Transactional
    public void delete(Long id, String userId) {
        repository.delete(ownedRecord(id, userId));
    }

    @Transactional(readOnly = true)
    public List<ExerciseRecommendationResponse> recommendations(LocalDate date, String userId) {
        List<ExerciseRecord> records = repository
                .findByUserIdAndExerciseDateOrderByStartTimeAscIdAsc(userId, date);
        int minutes = records.stream().mapToInt(ExerciseRecord::getDurationMinutes).sum();
        if (records.isEmpty()) {
            return List.of(
                    recommendation("after-meal-walk", "饭后舒缓走", ExerciseRecord.ExerciseType.walking,
                            ExerciseRecord.Intensity.low, 20, "低强度起步，按自己的节奏完成"),
                    recommendation("mobility", "核心激活", ExerciseRecord.ExerciseType.strength,
                            ExerciseRecord.Intensity.low, 12, "短时基础练习，可在家完成")
            );
        }
        if (minutes < 30) {
            return List.of(
                    recommendation("easy-stretch", "舒展拉伸", ExerciseRecord.ExerciseType.stretching,
                            ExerciseRecord.Intensity.low, 12, "今天已有运动，补充轻量舒展"),
                    recommendation("easy-walk", "轻快步行", ExerciseRecord.ExerciseType.walking,
                            ExerciseRecord.Intensity.low, 15, "根据今日记录提供的轻量选择")
            );
        }
        return List.of(
                recommendation("recovery-yoga", "放松瑜伽", ExerciseRecord.ExerciseType.yoga,
                        ExerciseRecord.Intensity.low, 12, "今天运动量已较充足，可选择轻量活动")
        );
    }

    private ExerciseRecommendationResponse recommendation(String id, String name,
                                                           ExerciseRecord.ExerciseType type,
                                                           ExerciseRecord.Intensity intensity,
                                                           int minutes, String reason) {
        return new ExerciseRecommendationResponse(id, name, type, intensity, intensityLabel(intensity), minutes,
                calculator.calculate(type, intensity, minutes), reason);
    }

    private void apply(ExerciseRecord record, ExerciseRecordRequest request) {
        record.setExerciseDate(request.exerciseDate());
        record.setExerciseType(request.exerciseType());
        record.setStartTime(request.startTime() == null ? LocalTime.now().withSecond(0).withNano(0) : request.startTime());
        record.setDurationMinutes(request.durationMinutes());
        record.setIntensity(request.intensity());
        record.setCaloriesBurned(request.caloriesBurned() == null
                ? calculator.calculate(request.exerciseType(), request.intensity(), request.durationMinutes())
                : request.caloriesBurned().setScale(1, RoundingMode.HALF_UP));
        record.setSource(request.source() == null ? ExerciseRecord.Source.MANUAL : request.source());
        record.setNote(request.note() == null ? null : request.note().trim());
    }

    private ExerciseRecord ownedRecord(Long id, String userId) {
        ExerciseRecord record = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EXERCISE_NOT_FOUND", "运动记录不存在"));
        if (!userId.equals(record.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "无权操作该运动记录");
        }
        return record;
    }

    private ExerciseResponse response(ExerciseRecord record) {
        return new ExerciseResponse(record.getId(), record.getExerciseDate(), record.getExerciseType(),
                typeLabel(record.getExerciseType()), record.getStartTime(), record.getDurationMinutes(),
                record.getIntensity(), intensityLabel(record.getIntensity()), display(record.getCaloriesBurned()),
                record.getSource(), record.getNote());
    }

    private String typeLabel(ExerciseRecord.ExerciseType type) {
        return switch (type) {
            case walking -> "户外快走";
            case running -> "跑步";
            case cycling -> "骑行";
            case strength -> "力量训练";
            case yoga -> "瑜伽";
            case stretching -> "拉伸";
            case other -> "其他运动";
        };
    }

    private String intensityLabel(ExerciseRecord.Intensity intensity) {
        return switch (intensity) {
            case low -> "低强度";
            case medium -> "中等强度";
            case high -> "高强度";
        };
    }

    private BigDecimal display(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
