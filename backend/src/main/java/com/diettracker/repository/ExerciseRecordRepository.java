package com.diettracker.repository;

import com.diettracker.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
    List<ExerciseRecord> findByUserIdAndExerciseDateOrderByStartTimeAscIdAsc(String userId, LocalDate date);
    List<ExerciseRecord> findByUserIdAndExerciseDateBetweenOrderByExerciseDateAscStartTimeAsc(
            String userId, LocalDate start, LocalDate end);
}
