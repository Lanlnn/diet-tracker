package com.diettracker.controller;

import com.diettracker.model.FoodCategory;
import com.diettracker.model.FoodItem;
import com.diettracker.service.MealRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final MealRecordService service;

    public FoodController(MealRecordService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<FoodCategory> getCategories() {
        return service.getAllCategories();
    }

    @GetMapping
    public List<FoodItem> getFoods(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return service.getFoodsByCategory(categoryId);
        }
        return service.getAllFoods();
    }

    @GetMapping("/search")
    public List<FoodItem> searchFood(@RequestParam String keyword) {
        return service.searchFood(keyword);
    }

    @PostMapping
    public ResponseEntity<FoodItem> addFood(@RequestBody FoodItem foodItem) {
        FoodItem saved = service.addFoodItem(foodItem);
        return ResponseEntity.ok(saved);
    }
}
