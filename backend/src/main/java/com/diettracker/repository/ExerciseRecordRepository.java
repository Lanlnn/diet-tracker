package com.diettracker.repository;

import com.diettracker.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
    List<ExerciseRecord> findByUserIdAndExerciseDateOrderByStartTimeAscIdAsc(String userId, LocalDate date);
    List<ExerciseRecord> findByUserIdAndExerciseDateBetweenOrderByExerciseDateAscStartTimeAsc(
            String userId, LocalDate start, LocalDate end);

    @Query("SELECT e.exerciseDate, COALESCE(SUM(e.caloriesBurned), 0) " +
           "FROM ExerciseRecord e " +
           "WHERE e.userId = :userId AND e.exerciseDate BETWEEN :start AND :end " +
           "GROUP BY e.exerciseDate ORDER BY e.exerciseDate")
    List<Object[]> sumCaloriesGroupByDate(@Param("userId") String userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    @Query("SELECT e.exerciseDate, COALESCE(SUM(e.caloriesBurned), 0) " +
           "FROM ExerciseRecord e " +
           "WHERE e.userId = :userId AND e.exerciseDate BETWEEN :start AND :end " +
           "GROUP BY e.exerciseDate ORDER BY e.exerciseDate")
    List<Object[]> summarizeCalendarByDate(@Param("userId") String userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);
}
