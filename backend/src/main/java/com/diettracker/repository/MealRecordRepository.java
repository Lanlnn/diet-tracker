package com.diettracker.repository;

import com.diettracker.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    @Query(value = "SELECT m.foodItem.id FROM MealRecord m WHERE m.userId = :userId " +
           "GROUP BY m.foodItem.id ORDER BY COUNT(m.id) DESC, MAX(m.recordTime) DESC",
           countQuery = "SELECT COUNT(DISTINCT m.foodItem.id) FROM MealRecord m WHERE m.userId = :userId")
    Page<Long> findCommonFoodIds(@Param("userId") String userId, Pageable pageable);

    @Query(value = "SELECT m.foodItem.id FROM MealRecord m WHERE m.userId = :userId " +
           "GROUP BY m.foodItem.id ORDER BY MAX(m.recordTime) DESC",
           countQuery = "SELECT COUNT(DISTINCT m.foodItem.id) FROM MealRecord m WHERE m.userId = :userId")
    Page<Long> findRecentFoodIds(@Param("userId") String userId, Pageable pageable);

    // Filter by user
    List<MealRecord> findByUserIdAndMealDateOrderByRecordTimeAsc(String userId, LocalDate mealDate);

    List<MealRecord> findByUserIdAndMealDateAndMealTypeOrderByRecordTimeAsc(
            String userId, LocalDate mealDate, MealRecord.MealType mealType);

    Optional<MealRecord> findByUserIdAndClientRequestId(String userId, String clientRequestId);

    List<MealRecord> findByUserIdAndMealDateBetweenOrderByMealDateAsc(String userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate = :date")
    Double sumCaloriesByDate(@Param("userId") String userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate BETWEEN :start AND :end")
    Double sumCaloriesByDateRange(@Param("userId") String userId,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);

    @Query("SELECT m.mealDate, COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate BETWEEN :start AND :end " +
           "GROUP BY m.mealDate ORDER BY m.mealDate")
    List<Object[]> sumCaloriesGroupByDate(@Param("userId") String userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    @Query("SELECT m.mealDate, " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0), " +
           "COUNT(DISTINCT m.mealType) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate BETWEEN :start AND :end " +
           "GROUP BY m.mealDate ORDER BY m.mealDate")
    List<Object[]> summarizeCalendarByDate(@Param("userId") String userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Query("SELECT m.mealDate, " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.proteinSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.fatSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.carbsSnapshot), 0) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate BETWEEN :start AND :end " +
           "GROUP BY m.mealDate ORDER BY m.mealDate")
    List<Object[]> sumNutritionGroupByDate(@Param("userId") String userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    // Stats query per user per date
    @Query("SELECT COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.caloriesSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.proteinSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.fatSnapshot), 0), " +
           "COALESCE(SUM(m.quantity / m.baseAmountSnapshot * m.carbsSnapshot), 0) " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate = :date")
    List<Object[]> sumNutritionByDate(@Param("userId") String userId, @Param("date") LocalDate date);
}
