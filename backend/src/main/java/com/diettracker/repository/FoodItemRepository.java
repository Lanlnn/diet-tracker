package com.diettracker.repository;

import com.diettracker.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByCategoryIdOrderByNameAsc(Long categoryId);
    List<FoodItem> findByNameContaining(String name);
}
