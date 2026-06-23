package com.diettracker.controller;

import com.diettracker.model.FoodCategory;
import com.diettracker.model.FoodItem;
import com.diettracker.service.MealRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private static final Logger log = LoggerFactory.getLogger(FoodController.class);

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
    public ResponseEntity<FoodItem> addFood(@RequestBody FoodItem foodItem, @RequestAttribute("userId") String userId) {
        log.info(">>> 收到添加食物请求: name='{}', unit='{}', calories={}, protein={}, fat={}, carbs={}, category={}",
                foodItem.getName(), foodItem.getUnit(), foodItem.getCalories(),
                foodItem.getProtein(), foodItem.getFat(), foodItem.getCarbs(),
                foodItem.getCategory());
        FoodItem saved = service.addFoodItem(foodItem, userId);
        log.info(">>> 保存后: id={}, name='{}'", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }
}
