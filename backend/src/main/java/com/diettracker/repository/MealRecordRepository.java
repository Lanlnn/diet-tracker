package com.diettracker.repository;

import com.diettracker.model.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {
    List<MealRecord> findByMealDateOrderByRecordTimeAsc(LocalDate mealDate);

    List<MealRecord> findByMealDateBetweenOrderByMealDateAsc(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(m.quantity * f.calories), 0) FROM MealRecord m JOIN m.foodItem f WHERE m.mealDate = :date")
    Double sumCaloriesByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.quantity * f.calories), 0) FROM MealRecord m JOIN m.foodItem f WHERE m.mealDate BETWEEN :start AND :end")
    Double sumCaloriesByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT m.mealDate, COALESCE(SUM(m.quantity * f.calories), 0) FROM MealRecord m JOIN m.foodItem f WHERE m.mealDate BETWEEN :start AND :end GROUP BY m.mealDate ORDER BY m.mealDate")
    List<Object[]> sumCaloriesGroupByDate(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
