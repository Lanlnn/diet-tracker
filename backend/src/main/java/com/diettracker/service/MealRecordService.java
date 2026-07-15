package com.diettracker.service;

import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.dto.CreateFoodRequest;
import com.diettracker.dto.CreateMealRecordRequest;
import com.diettracker.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;

@Service
public class MealRecordService {

    private static final Logger log = LoggerFactory.getLogger(MealRecordService.class);

    private final MealRecordRepository mealRecordRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    public MealRecordService(MealRecordRepository mealRecordRepository,
                             FoodItemRepository foodItemRepository,
                             FoodCategoryRepository foodCategoryRepository) {
        this.mealRecordRepository = mealRecordRepository;
        this.foodItemRepository = foodItemRepository;
        this.foodCategoryRepository = foodCategoryRepository;
    }

    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAllByOrderBySortOrderAsc();
    }

    public List<FoodItem> getFoodsByCategory(Long categoryId, String userId) {
        return foodItemRepository.findFoodsByCategory(categoryId, userId);
    }

    public List<FoodItem> searchFood(String keyword, String userId) {
        return foodItemRepository.searchFoods(keyword, userId);
    }

    public List<FoodItem> getAllFoods(String userId) {
        return foodItemRepository.findAllFoods(userId);
    }

    public MealRecord addRecord(CreateMealRecordRequest request, String userId) {
        FoodItem managed = foodItemRepository.findById(request.foodItemId())
                .filter(food -> food.getUserId() == null || userId.equals(food.getUserId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "食品不存在"));
        MealRecord record = new MealRecord();
        record.setMealDate(request.mealDate());
        record.setMealType(request.mealType());
        record.setFoodItem(managed);
        record.setQuantity(request.quantity());
        record.setUnit(request.unit() == null ? managed.getUnit() : request.unit());
        record.setRecordTime(request.recordTime());
        record.setNote(request.note());
        record.setUserId(userId);
        return mealRecordRepository.save(record);
    }

    public List<MealRecord> getRecordsByDate(LocalDate date, String userId) {
        return mealRecordRepository.findByUserIdAndMealDateOrderByRecordTimeAsc(userId, date);
    }

    public List<MealRecord> getRecordsByDateRange(LocalDate start, LocalDate end, String userId) {
        return mealRecordRepository.findByUserIdAndMealDateBetweenOrderByMealDateAsc(userId, start, end);
    }

    public void deleteRecord(Long id, String userId) {
        MealRecord record = mealRecordRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RECORD_NOT_FOUND", "记录不存在"));
        if (!userId.equals(record.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "无权操作该记录");
        }
        mealRecordRepository.delete(record);
    }

    public Map<String, Object> getDailyStats(LocalDate date, String userId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<MealRecord> records = mealRecordRepository.findByUserIdAndMealDateOrderByRecordTimeAsc(userId, date);
        double totalCalories = 0, totalProtein = 0, totalFat = 0, totalCarbs = 0;

        for (MealRecord r : records) {
            double qty = r.getQuantity().doubleValue();
            totalCalories += r.getFoodItem().getCalories().doubleValue() * qty;
            totalProtein += r.getFoodItem().getProtein().doubleValue() * qty;
            totalFat += r.getFoodItem().getFat().doubleValue() * qty;
            totalCarbs += r.getFoodItem().getCarbs().doubleValue() * qty;
        }

        stats.put("date", date.toString());
        stats.put("totalCalories", Math.round(totalCalories * 100) / 100.0);
        stats.put("totalProtein", Math.round(totalProtein * 100) / 100.0);
        stats.put("totalFat", Math.round(totalFat * 100) / 100.0);
        stats.put("totalCarbs", Math.round(totalCarbs * 100) / 100.0);
        stats.put("recordCount", records.size());
        return stats;
    }

    public Map<String, Object> getWeeklyStats(LocalDate date, String userId) {
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = date.with(DayOfWeek.SUNDAY);
        List<Object[]> dailyData = mealRecordRepository.sumNutritionGroupByDate(userId, weekStart, weekEnd);

        List<Map<String, Object>> dailyList = new ArrayList<>();
        double totalCalories = 0, totalProtein = 0, totalFat = 0, totalCarbs = 0;
        int daysWithData = 0;

        for (Object[] row : dailyData) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", ((LocalDate) row[0]).toString());
            double cal = ((Number) row[1]).doubleValue();
            double pro = ((Number) row[2]).doubleValue();
            double fat = ((Number) row[3]).doubleValue();
            double carb = ((Number) row[4]).doubleValue();
            day.put("calories", Math.round(cal * 100) / 100.0);
            day.put("protein", Math.round(pro * 100) / 100.0);
            day.put("fat", Math.round(fat * 100) / 100.0);
            day.put("carbs", Math.round(carb * 100) / 100.0);
            if (cal > 0) daysWithData++;
            totalCalories += cal;
            totalProtein += pro;
            totalFat += fat;
            totalCarbs += carb;
            dailyList.add(day);
        }

        int count = Math.max(daysWithData, 1);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", weekStart.toString());
        result.put("endDate", weekEnd.toString());
        result.put("dailyData", dailyList);
        result.put("avgCalories", Math.round(totalCalories / count));
        result.put("avgProtein", Math.round(totalProtein / count * 10) / 10.0);
        result.put("avgFat", Math.round(totalFat / count * 10) / 10.0);
        result.put("avgCarbs", Math.round(totalCarbs / count * 10) / 10.0);
        return result;
    }

    public FoodItem addFoodItem(CreateFoodRequest request, String userId) {
        FoodCategory category;
        if (request.categoryId() == null) {
            category = foodCategoryRepository.findAllByOrderBySortOrderAsc()
                    .stream().findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "CATEGORY_REQUIRED", "请先初始化食品分类"));
        } else {
            category = foodCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "食品分类不存在"));
        }
        FoodItem foodItem = new FoodItem();
        foodItem.setName(request.name().trim());
        foodItem.setCategory(category);
        foodItem.setBaseAmount(request.baseAmount() == null ? new BigDecimal("100") : request.baseAmount());
        foodItem.setBaseUnit(request.baseUnit() == null ? "g" : request.baseUnit().trim());
        foodItem.setServingAmount(request.servingAmount());
        foodItem.setServingUnit(request.servingUnit());
        foodItem.setUnit(request.unit() == null ? foodItem.getBaseUnit() : request.unit());
        foodItem.setCalories(request.calories() == null ? BigDecimal.ZERO : request.calories());
        foodItem.setProtein(request.protein() == null ? BigDecimal.ZERO : request.protein());
        foodItem.setFat(request.fat() == null ? BigDecimal.ZERO : request.fat());
        foodItem.setCarbs(request.carbs() == null ? BigDecimal.ZERO : request.carbs());
        foodItem.setSource("USER_CUSTOM");
        foodItem.setUserId(userId);
        return foodItemRepository.save(foodItem);
    }
}
