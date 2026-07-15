package com.diettracker.repository;

import com.diettracker.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByCategoryIdOrderByNameAsc(Long categoryId);

    @Query("SELECT f FROM FoodItem f WHERE f.category.id = :categoryId " +
           "AND (f.userId IS NULL OR f.userId = :userId) ORDER BY f.name ASC")
    List<FoodItem> findFoodsByCategory(@Param("categoryId") Long categoryId, @Param("userId") String userId);

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (f.userId IS NULL OR f.userId = :userId) ORDER BY f.name")
    List<FoodItem> searchFoods(@Param("keyword") String keyword, @Param("userId") String userId);

    @Query("SELECT f FROM FoodItem f WHERE f.userId IS NULL OR f.userId = :userId ORDER BY f.name ASC")
    List<FoodItem> findAllFoods(@Param("userId") String userId);
}
