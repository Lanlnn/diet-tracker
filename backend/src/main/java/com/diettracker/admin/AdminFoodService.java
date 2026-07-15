package com.diettracker.admin;

import com.diettracker.admin.dto.*;
import com.diettracker.api.ApiException;
import com.diettracker.dto.NutritionCalculationResponse;
import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.service.NutritionCalculationService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminFoodService {
    private static final int MAX_PAGE_SIZE = 50;
    private final FoodItemRepository foods;
    private final FoodCategoryRepository categories;
    private final NutritionCalculationService nutrition;
    private final AdminUserRefService userRefs;

    public AdminFoodService(FoodItemRepository foods, FoodCategoryRepository categories,
                            NutritionCalculationService nutrition, AdminUserRefService userRefs) {
        this.foods = foods; this.categories = categories; this.nutrition = nutrition; this.userRefs = userRefs;
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminFoodResponse> list(String keyword, Long categoryId, String source,
                                                     int page, int size, String sort, String direction) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw bad("INVALID_PAGE", "分页参数不合法");
        String property = switch (sort == null ? "updatedAt" : sort) {
            case "name" -> "name"; case "calories" -> "calories"; case "updatedAt" -> "updatedAt";
            default -> throw bad("INVALID_SORT", "排序字段不合法");
        };
        Sort.Direction sortDirection;
        try { sortDirection = Sort.Direction.fromString(direction == null ? "desc" : direction); }
        catch (IllegalArgumentException exception) { throw bad("INVALID_SORT", "排序方向不合法"); }
        String cleanKeyword = clean(keyword);
        String cleanSource = clean(source);
        Page<FoodItem> result = foods.findAdminSystemFoods(cleanKeyword, categoryId,
                cleanSource == null ? null : cleanSource.toUpperCase(Locale.ROOT),
                PageRequest.of(page, size, Sort.by(sortDirection, property)));
        return new AdminPageResponse<>(result.map(AdminFoodResponse::from).getContent(), page, size,
                result.getTotalElements(), result.hasNext());
    }

    @Transactional(readOnly = true)
    public AdminFoodResponse getSystemFood(Long id) { return AdminFoodResponse.from(requireSystemFood(id)); }

    @Transactional
    public AdminFoodResponse create(AdminFoodInput input) {
        FoodItem food = new FoodItem(); apply(food, input); food.setUserId(null);
        return AdminFoodResponse.from(foods.save(food));
    }

    @Transactional
    public AdminFoodResponse update(Long id, AdminFoodInput input) {
        FoodItem food = requireSystemFood(id); apply(food, input);
        return AdminFoodResponse.from(foods.save(food));
    }

    public NutritionCalculationResponse preview(AdminFoodPreviewRequest request) {
        AdminFoodInput food = request.food();
        return nutrition.preview(food.name().trim(), food.baseAmount(), food.baseUnit().trim(),
                food.servingAmount(), clean(food.servingUnit()), food.calories(), food.protein(),
                food.fat(), food.carbs(), request.amount());
    }

    @Transactional(readOnly = true)
    public List<AdminFoodCategoryResponse> listCategories() {
        return categories.findAllByOrderBySortOrderAsc().stream()
                .map(value -> AdminFoodCategoryResponse.from(value, foods.countByCategoryIdAndUserIdIsNull(value.getId())))
                .toList();
    }

    @Transactional
    public AdminFoodCategoryResponse createCategory(AdminFoodCategoryInput input) {
        if (categories.existsByNameIgnoreCase(input.name().trim())) throw bad("CATEGORY_NAME_EXISTS", "食品分类名称已存在");
        FoodCategory category = new FoodCategory(); apply(category, input);
        category = categories.save(category);
        return AdminFoodCategoryResponse.from(category, 0);
    }

    @Transactional
    public AdminFoodCategoryResponse updateCategory(Long id, AdminFoodCategoryInput input) {
        FoodCategory category = categories.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "食品分类不存在"));
        categories.findByNameIgnoreCase(input.name().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw bad("CATEGORY_NAME_EXISTS", "食品分类名称已存在"); });
        apply(category, input); category = categories.save(category);
        return AdminFoodCategoryResponse.from(category, foods.countByCategoryIdAndUserIdIsNull(id));
    }

    @Transactional
    public void deleteCategory(Long id) {
        FoodCategory category = categories.findById(id).orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "食品分类不存在"));
        long count = foods.countByCategoryIdAndUserIdIsNull(id);
        if (count > 0) throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_IN_USE", "分类仍被系统食品引用，禁止删除");
        categories.delete(category);
    }

    @Transactional(readOnly = true)
    public CustomFoodDiagnosticResponse diagnoseCustomFood(String userRef, Long foodId) {
        FoodItem food = foods.findByIdAndUserIdIsNotNull(foodId)
                .orElseThrow(() -> notFound("CUSTOM_FOOD_NOT_FOUND", "用户自定义食品不存在"));
        String expected = userRefs.reference(food.getUserId());
        if (!expected.equals(userRef)) throw notFound("CUSTOM_FOOD_NOT_FOUND", "用户自定义食品不存在");
        return CustomFoodDiagnosticResponse.from(food, expected);
    }

    private FoodItem requireSystemFood(Long id) {
        return foods.findById(id).filter(food -> food.getUserId() == null)
                .orElseThrow(() -> notFound("SYSTEM_FOOD_NOT_FOUND", "系统食品不存在"));
    }
    private void apply(FoodItem food, AdminFoodInput input) {
        FoodCategory category = categories.findById(input.categoryId())
                .orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "食品分类不存在"));
        food.setName(input.name().trim()); food.setCategory(category); food.setBaseAmount(input.baseAmount());
        food.setBaseUnit(input.baseUnit().trim()); food.setServingAmount(input.servingAmount());
        food.setServingUnit(clean(input.servingUnit())); food.setUnit(food.getServingUnit() == null ? food.getBaseUnit() : food.getServingUnit());
        food.setCalories(input.calories()); food.setProtein(input.protein()); food.setFat(input.fat()); food.setCarbs(input.carbs());
        food.setSource(input.source());
    }
    private void apply(FoodCategory category, AdminFoodCategoryInput input) {
        category.setName(input.name().trim()); category.setIcon(clean(input.icon())); category.setSortOrder(input.sortOrder());
    }
    private String clean(String value) { if (value == null || value.trim().isEmpty()) return null; return value.trim(); }
    private ApiException bad(String code, String message) { return new ApiException(HttpStatus.BAD_REQUEST, code, message); }
    private ApiException notFound(String code, String message) { return new ApiException(HttpStatus.NOT_FOUND, code, message); }
}
