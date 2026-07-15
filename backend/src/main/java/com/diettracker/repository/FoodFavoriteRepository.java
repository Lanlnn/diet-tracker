package com.diettracker.repository;

import com.diettracker.entity.FoodFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FoodFavoriteRepository extends JpaRepository<FoodFavorite, Long> {
    boolean existsByUserIdAndFoodItemId(String userId, Long foodItemId);

    @Modifying
    long deleteByUserIdAndFoodItemId(String userId, Long foodItemId);

    @Query("select favorite.foodItem.id from FoodFavorite favorite where favorite.userId = :userId " +
            "order by favorite.createdAt desc")
    List<Long> findFoodIds(@Param("userId") String userId);

    @Query(value = "select favorite.foodItem.id from FoodFavorite favorite where favorite.userId = :userId " +
            "order by favorite.createdAt desc",
            countQuery = "select count(favorite.id) from FoodFavorite favorite where favorite.userId = :userId")
    Page<Long> findFoodIds(@Param("userId") String userId, Pageable pageable);
}
