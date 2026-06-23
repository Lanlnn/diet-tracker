package com.diettracker.service;

import com.diettracker.model.FoodCategory;
import com.diettracker.model.FoodItem;
import com.diettracker.model.MealRecord;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
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

    public MealRecord addRecord(MealRecord record, String userId) {
        if (record.getFoodItem() != null && record.getFoodItem().getId() != null) {
            FoodItem managed = foodItemRepository.findById(record.getFoodItem().getId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + record.getFoodItem().getId()));
            record.setFoodItem(managed);
        }
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
                .orElseThrow(() -> new RuntimeException("Record not found: " + id));
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this record");
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

    public FoodItem addFoodItem(FoodItem foodItem, String userId) {
        if (foodItem.getCategory() == null || foodItem.getCategory().getId() == null) {
            FoodCategory defaultCat = foodCategoryRepository.findAllByOrderBySortOrderAsc()
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No categories available"));
            foodItem.setCategory(defaultCat);
        }
        if (foodItem.getCalories() == null) foodItem.setCalories(BigDecimal.ZERO);
        if (foodItem.getProtein() == null) foodItem.setProtein(BigDecimal.ZERO);
        if (foodItem.getFat() == null) foodItem.setFat(BigDecimal.ZERO);
        if (foodItem.getCarbs() == null) foodItem.setCarbs(BigDecimal.ZERO);
        if (foodItem.getUnit() == null) foodItem.setUnit("份");
        foodItem.setUserId(userId);
        return foodItemRepository.save(foodItem);
    }
}
