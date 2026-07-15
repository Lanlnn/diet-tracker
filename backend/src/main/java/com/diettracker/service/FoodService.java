package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.CreateFoodRequest;
import com.diettracker.dto.FoodResponse;
import com.diettracker.dto.PageResponse;
import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodFavorite;
import com.diettracker.entity.FoodItem;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodFavoriteRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FoodService {
    private static final int MAX_PAGE_SIZE = 50;

    private final FoodItemRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;
    private final FoodFavoriteRepository favoriteRepository;
    private final MealRecordRepository mealRecordRepository;

    public FoodService(FoodItemRepository foodRepository,
                       FoodCategoryRepository categoryRepository,
                       FoodFavoriteRepository favoriteRepository,
                       MealRecordRepository mealRecordRepository) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.favoriteRepository = favoriteRepository;
        this.mealRecordRepository = mealRecordRepository;
    }

    public List<FoodCategory> getCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    public PageResponse<FoodResponse> listFoods(String scope, Long categoryId, int page, int size, String userId) {
        Pageable pageable = pageable(page, size);
        String normalizedScope = scope == null || scope.isBlank() ? "common" : scope.trim().toLowerCase();
        if (categoryId != null || "all".equals(normalizedScope)) {
            Page<FoodItem> foods = foodRepository.findVisibleFoods(
                    userId, categoryId, PageRequest.of(page, size, Sort.by("name").ascending()));
            return response(foods, userId);
        }

        Page<Long> ids = switch (normalizedScope) {
            case "common" -> mealRecordRepository.findCommonFoodIds(userId, pageable);
            case "recent" -> mealRecordRepository.findRecentFoodIds(userId, pageable);
            case "favorite" -> favoriteRepository.findFoodIds(userId, pageable);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SCOPE", "不支持的食品列表类型");
        };
        if ("common".equals(normalizedScope) && ids.getTotalElements() == 0) {
            Page<FoodItem> recommendations = foodRepository.findVisibleFoods(
                    userId, null, PageRequest.of(page, size, Sort.by("name").ascending()));
            return response(recommendations, userId);
        }
        return responseFromIds(ids, userId);
    }

    public PageResponse<FoodResponse> search(String keyword, int page, int size, String userId) {
        String cleaned = keyword == null ? "" : keyword.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_KEYWORD", "请输入搜索关键词");
        }
        if (cleaned.length() > 50) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "KEYWORD_TOO_LONG", "搜索关键词不能超过 50 个字符");
        }
        Page<FoodItem> foods = foodRepository.searchVisibleFoods(
                cleaned, userId, PageRequest.of(page, size, Sort.by("name").ascending()));
        return response(foods, userId);
    }

    @Transactional
    public FoodResponse create(CreateFoodRequest request, String userId) {
        FoodCategory category = request.categoryId() == null
                ? categoryRepository.findAllByOrderBySortOrderAsc().stream()
                    .filter(item -> "其他".equals(item.getName()))
                    .findFirst()
                    .orElseGet(() -> categoryRepository.findAllByOrderBySortOrderAsc().stream().findFirst()
                            .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "CATEGORY_REQUIRED", "请先初始化食品分类")))
                : categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "食品分类不存在"));

        FoodItem food = new FoodItem();
        food.setName(request.name().trim());
        food.setCategory(category);
        food.setBaseAmount(request.baseAmount() == null ? new BigDecimal("100") : request.baseAmount());
        food.setBaseUnit(normalizeUnit(request.baseUnit(), "g"));
        food.setServingAmount(request.servingAmount());
        food.setServingUnit(normalizeUnit(request.servingUnit(), null));
        food.setUnit(food.getServingUnit() == null ? food.getBaseUnit() : food.getServingUnit());
        food.setCalories(orZero(request.calories()));
        food.setProtein(orZero(request.protein()));
        food.setFat(orZero(request.fat()));
        food.setCarbs(orZero(request.carbs()));
        food.setSource("USER_CUSTOM");
        food.setUserId(userId);
        return FoodResponse.from(foodRepository.save(food), false);
    }

    @Transactional
    public FoodResponse setFavorite(Long foodId, boolean favorite, String userId) {
        FoodItem food = visibleFood(foodId, userId);
        boolean exists = favoriteRepository.existsByUserIdAndFoodItemId(userId, foodId);
        if (favorite && !exists) {
            FoodFavorite value = new FoodFavorite();
            value.setUserId(userId);
            value.setFoodItem(food);
            favoriteRepository.save(value);
        } else if (!favorite && exists) {
            favoriteRepository.deleteByUserIdAndFoodItemId(userId, foodId);
        }
        return FoodResponse.from(food, favorite);
    }

    private FoodItem visibleFood(Long foodId, String userId) {
        return foodRepository.findById(foodId)
                .filter(food -> food.getUserId() == null || userId.equals(food.getUserId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "食品不存在"));
    }

    private PageResponse<FoodResponse> response(Page<FoodItem> page, String userId) {
        Set<Long> favoriteIds = new HashSet<>(favoriteRepository.findFoodIds(userId));
        List<FoodResponse> items = page.getContent().stream()
                .map(food -> FoodResponse.from(food, favoriteIds.contains(food.getId())))
                .toList();
        return new PageResponse<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.hasNext());
    }

    private PageResponse<FoodResponse> responseFromIds(Page<Long> idPage, String userId) {
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageResponse<>(List.of(), idPage.getNumber(), idPage.getSize(), idPage.getTotalElements(), false);
        }
        Map<Long, FoodItem> byId = new HashMap<>();
        foodRepository.findVisibleFoodsByIds(ids, userId).forEach(food -> byId.put(food.getId(), food));
        Set<Long> favoriteIds = new HashSet<>(favoriteRepository.findFoodIds(userId));
        List<FoodResponse> items = new ArrayList<>();
        for (Long id : ids) {
            FoodItem food = byId.get(id);
            if (food != null) items.add(FoodResponse.from(food, favoriteIds.contains(id)));
        }
        return new PageResponse<>(items, idPage.getNumber(), idPage.getSize(), idPage.getTotalElements(), idPage.hasNext());
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "分页参数不合法");
        }
        return PageRequest.of(page, size);
    }

    private BigDecimal orZero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private String normalizeUnit(String value, String fallback) {
        String cleaned = value == null ? "" : value.trim();
        return cleaned.isEmpty() ? fallback : cleaned;
    }
}
