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
import java.util.*;

@Service
public class MealRecordService {

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

    public List<FoodItem> getFoodsByCategory(Long categoryId) {
        return foodItemRepository.findByCategoryIdOrderByNameAsc(categoryId);
    }

    public List<FoodItem> searchFood(String keyword) {
        return foodItemRepository.findByNameContaining(keyword);
    }

    public List<FoodItem> getAllFoods() {
        return foodItemRepository.findAll();
    }

    public MealRecord addRecord(MealRecord record) {
        if (record.getFoodItem() != null && record.getFoodItem().getId() != null) {
            FoodItem managed = foodItemRepository.findById(record.getFoodItem().getId())
                    .orElseThrow(() -> new RuntimeException("Food not found: " + record.getFoodItem().getId()));
            record.setFoodItem(managed);
        }
        return mealRecordRepository.save(record);
    }

    public List<MealRecord> getRecordsByDate(LocalDate date) {
        return mealRecordRepository.findByMealDateOrderByRecordTimeAsc(date);
    }

    public List<MealRecord> getRecordsByDateRange(LocalDate start, LocalDate end) {
        return mealRecordRepository.findByMealDateBetweenOrderByMealDateAsc(start, end);
    }

    public void deleteRecord(Long id) {
        mealRecordRepository.deleteById(id);
    }

    public Map<String, Object> getDailyStats(LocalDate date) {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<MealRecord> records = mealRecordRepository.findByMealDateOrderByRecordTimeAsc(date);
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

    public Map<String, Object> getWeeklyStats(LocalDate date) {
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = date.with(DayOfWeek.SUNDAY);
        List<Object[]> dailyData = mealRecordRepository.sumCaloriesGroupByDate(weekStart, weekEnd);

        List<Map<String, Object>> dailyList = new ArrayList<>();
        for (Object[] row : dailyData) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", ((LocalDate) row[0]).toString());
            Number calVal = (Number) row[1];
            day.put("calories", Math.round(calVal.doubleValue() * 100) / 100.0);
            dailyList.add(day);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", weekStart.toString());
        result.put("endDate", weekEnd.toString());
        result.put("dailyData", dailyList);
        return result;
    }

    public FoodItem addFoodItem(FoodItem foodItem) {
        if (foodItem.getCategory() == null || foodItem.getCategory().getId() == null) {
            FoodCategory defaultCat = foodCategoryRepository.findById(7L)
                    .orElseThrow(() -> new RuntimeException("Default category not found"));
            foodItem.setCategory(defaultCat);
        }
        if (foodItem.getCalories() == null) foodItem.setCalories(java.math.BigDecimal.ZERO);
        if (foodItem.getProtein() == null) foodItem.setProtein(java.math.BigDecimal.ZERO);
        if (foodItem.getFat() == null) foodItem.setFat(java.math.BigDecimal.ZERO);
        if (foodItem.getCarbs() == null) foodItem.setCarbs(java.math.BigDecimal.ZERO);
        if (foodItem.getUnit() == null) foodItem.setUnit("份");
        return foodItemRepository.save(foodItem);
    }
}
