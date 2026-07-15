package com.diettracker.controller;

import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.service.MealRecordService;
import com.diettracker.dto.CreateFoodRequest;
import jakarta.validation.Valid;
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
    public List<FoodItem> getFoods(
            @RequestParam(required = false) Long categoryId,
            @RequestAttribute("userId") String userId) {
        if (categoryId != null) {
            return service.getFoodsByCategory(categoryId, userId);
        }
        return service.getAllFoods(userId);
    }

    @GetMapping("/search")
    public List<FoodItem> searchFood(@RequestParam String keyword, @RequestAttribute("userId") String userId) {
        return service.searchFood(keyword, userId);
    }

    @PostMapping
    public ResponseEntity<FoodItem> addFood(@Valid @RequestBody CreateFoodRequest request,
                                             @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(service.addFoodItem(request, userId));
    }
}
