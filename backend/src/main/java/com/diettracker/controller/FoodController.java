package com.diettracker.controller;

import com.diettracker.entity.FoodCategory;
import com.diettracker.service.FoodService;
import com.diettracker.dto.CreateFoodRequest;
import com.diettracker.dto.FavoriteFoodRequest;
import com.diettracker.dto.FoodResponse;
import com.diettracker.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService service;

    public FoodController(FoodService service) {
        this.service = service;
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
