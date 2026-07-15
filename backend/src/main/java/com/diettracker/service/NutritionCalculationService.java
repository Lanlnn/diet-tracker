package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.api.FieldValidationException;
import com.diettracker.dto.NutritionCalculationResponse;
import com.diettracker.entity.FoodItem;
import com.diettracker.repository.FoodItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NutritionCalculationService {
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000");

    private final FoodItemRepository foodRepository;

    public NutritionCalculationService(FoodItemRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public NutritionCalculationResponse calculate(Long foodId, BigDecimal amount, String userId) {
        FoodItem food = foodRepository.findById(foodId)
                .filter(item -> item.getUserId() == null || userId.equals(item.getUserId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "食品不存在"));
        return calculateValues(food.getId(), food.getName(), food.getBaseAmount(), food.getBaseUnit(),
                food.getServingAmount(), food.getServingUnit(), food.getCalories(), food.getProtein(),
                food.getFat(), food.getCarbs(), amount);
    }

    public NutritionCalculationResponse preview(String name, BigDecimal baseAmount, String baseUnit,
                                                 BigDecimal servingAmount, String servingUnit,
                                                 BigDecimal calories, BigDecimal protein,
                                                 BigDecimal fat, BigDecimal carbs, BigDecimal amount) {
        return calculateValues(null, name, baseAmount, baseUnit, servingAmount, servingUnit,
                calories, protein, fat, carbs, amount);
    }

    private NutritionCalculationResponse calculateValues(Long id, String name, BigDecimal baseAmount, String baseUnit,
                                                          BigDecimal servingAmount, String servingUnit,
                                                          BigDecimal calories, BigDecimal protein, BigDecimal fat,
                                                          BigDecimal carbs, BigDecimal amount) {
        validateAmount(amount);
        validateFoodBasis(baseAmount, baseUnit, calories, protein, fat, carbs);
        BigDecimal ratio = amount.divide(baseAmount, 12, RoundingMode.HALF_UP);
        return new NutritionCalculationResponse(
                id, name, baseAmount, baseUnit, servingAmount, servingUnit, amount.stripTrailingZeros(), "g",
                scaled(calories, ratio, 0), scaled(protein, ratio, 1),
                scaled(fat, ratio, 1), scaled(carbs, ratio, 1));
    }

    private void validateAmount(BigDecimal amount) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (amount == null) fields.put("amount", "请输入食用重量");
        else if (amount.compareTo(MIN_AMOUNT) < 0) fields.put("amount", "食用重量不能小于 1g");
        else if (amount.compareTo(MAX_AMOUNT) > 0) fields.put("amount", "食用重量不能超过 10000g");
        else if (Math.max(amount.stripTrailingZeros().scale(), 0) > 1) fields.put("amount", "食用重量最多保留 1 位小数");
        if (!fields.isEmpty()) throw new FieldValidationException("INVALID_NUTRITION_INPUT", "营养计算参数不合法", fields);
    }

    private void validateFoodBasis(FoodItem food) {
        validateFoodBasis(food.getBaseAmount(), food.getBaseUnit(), food.getCalories(), food.getProtein(), food.getFat(), food.getCarbs());
    }

    private void validateFoodBasis(BigDecimal baseAmount, String baseUnit, BigDecimal calories,
                                   BigDecimal protein, BigDecimal fat, BigDecimal carbs) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (baseAmount == null || baseAmount.signum() <= 0) {
            fields.put("baseAmount", "食品缺少有效的营养基准");
        }
        if (!"g".equalsIgnoreCase(baseUnit)) {
            fields.put("baseUnit", "该食品不是按克记录，暂不支持重量计算");
        }
        validateNonNegative(fields, "calories", "热量", calories);
        validateNonNegative(fields, "protein", "蛋白质", protein);
        validateNonNegative(fields, "fat", "脂肪", fat);
        validateNonNegative(fields, "carbs", "碳水", carbs);
        if (!fields.isEmpty()) throw new FieldValidationException("INVALID_FOOD_BASIS", "食品营养基准不合法", fields);
    }

    private void validateNonNegative(Map<String, String> fields, String field, String label, BigDecimal value) {
        if (value == null) fields.put(field, label + "数据缺失");
        else if (value.signum() < 0) fields.put(field, label + "不能小于 0");
    }

    private BigDecimal scaled(BigDecimal baseValue, BigDecimal ratio, int scale) {
        return baseValue.multiply(ratio).setScale(scale, RoundingMode.HALF_UP);
    }
}
