package com.diettracker.controller;

import com.diettracker.entity.FoodCategory;
import com.diettracker.service.FoodService;
import com.diettracker.dto.CreateFoodRequest;
import com.diettracker.dto.FavoriteFoodRequest;
import com.diettracker.dto.FoodResponse;
import com.diettracker.dto.PageResponse;
import com.diettracker.dto.CalculateNutritionRequest;
import com.diettracker.dto.NutritionCalculationResponse;
import com.diettracker.service.NutritionCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService service;
    private final NutritionCalculationService nutritionCalculationService;

    public FoodController(FoodService service,
                          NutritionCalculationService nutritionCalculationService) {
        this.service = service;
        this.nutritionCalculationService = nutritionCalculationService;
    }

    @GetMapping("/categories")
    public List<FoodCategory> getCategories() {
        return service.getCategories();
    }

    @GetMapping
    public PageResponse<FoodResponse> getFoods(
            @RequestParam(defaultValue = "common") String scope,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") String userId) {
        return service.listFoods(scope, categoryId, page, size, userId);
    }

    @GetMapping("/search")
    public PageResponse<FoodResponse> searchFood(@RequestParam String keyword,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestAttribute("userId") String userId) {
        return service.search(keyword, page, size, userId);
    }

    @GetMapping("/{foodId}")
    public FoodResponse getFood(@PathVariable Long foodId,
                                @RequestAttribute("userId") String userId) {
        return service.getFood(foodId, userId);
    }

    @PostMapping("/{foodId}/calculate")
    public NutritionCalculationResponse calculate(
            @PathVariable Long foodId,
            @Valid @RequestBody CalculateNutritionRequest request,
            @RequestAttribute("userId") String userId) {
        return nutritionCalculationService.calculate(foodId, request.amount(), userId);
    }

    @PostMapping
    public ResponseEntity<FoodResponse> addFood(@Valid @RequestBody CreateFoodRequest request,
                                             @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(service.create(request, userId));
    }

    @PutMapping("/{foodId}/favorite")
    public FoodResponse setFavorite(@PathVariable Long foodId,
                                    @RequestBody FavoriteFoodRequest request,
                                    @RequestAttribute("userId") String userId) {
        return service.setFavorite(foodId, request.favorite(), userId);
    }
}
