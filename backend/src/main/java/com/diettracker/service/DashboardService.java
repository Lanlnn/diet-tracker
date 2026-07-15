package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.DashboardTodayResponse;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.ExerciseRecord;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.entity.User;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    static final int DEFAULT_CALORIE_GOAL = 1800;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(1);

    private final MealRecordRepository mealRecordRepository;
    private final UserRepository userRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;

    public DashboardService(MealRecordRepository mealRecordRepository, UserRepository userRepository,
                            ExerciseRecordRepository exerciseRecordRepository) {
        this.mealRecordRepository = mealRecordRepository;
        this.userRepository = userRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardTodayResponse getToday(LocalDate date, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
        List<MealRecord> records = mealRecordRepository
                .findByUserIdAndMealDateOrderByRecordTimeAsc(userId, date);

        int calorieGoal = user.getDailyCalorieGoal() == null
                ? DEFAULT_CALORIE_GOAL : user.getDailyCalorieGoal();
        String goalSource = user.getDailyCalorieGoal() == null ? "DEFAULT" : "USER";
        Totals totals = aggregate(records);
        BigDecimal goal = BigDecimal.valueOf(calorieGoal);
        BigDecimal remaining = goal.subtract(totals.calories()).max(BigDecimal.ZERO);
        BigDecimal exceeded = totals.calories().subtract(goal).max(BigDecimal.ZERO);

        List<ExerciseRecord> exerciseRecords = exerciseRecordRepository
                .findByUserIdAndExerciseDateOrderByStartTimeAscIdAsc(userId, date);
        BigDecimal exerciseCalories = exerciseRecords.stream().map(ExerciseRecord::getCaloriesBurned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DashboardTodayResponse.ExerciseSummary exercise = new DashboardTodayResponse.ExerciseSummary(
                exerciseRecords.isEmpty() ? "empty" : "success",
                exerciseRecords.size(),
                exerciseRecords.stream().mapToInt(ExerciseRecord::getDurationMinutes).sum(),
                display(exerciseCalories));

        return new DashboardTodayResponse(
                date,
                calorieGoal,
                goalSource,
                display(totals.calories()),
                display(remaining),
                display(exceeded),
                display(exerciseCalories),
                display(totals.calories().subtract(exerciseCalories)),
                nutrition(totals, calorieGoal),
                exercise,
                mealSummaries(records),
                advice(records.isEmpty(), exceeded, remaining)
        );
    }

    private Totals aggregate(List<MealRecord> records) {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        for (MealRecord record : records) {
            BigDecimal base = record.getBaseAmountSnapshot();
            if (base == null || base.signum() <= 0) continue;
            BigDecimal ratio = record.getQuantity().divide(base, 8, RoundingMode.HALF_UP);
            calories = calories.add(record.getCaloriesSnapshot().multiply(ratio));
            protein = protein.add(record.getProteinSnapshot().multiply(ratio));
            fat = fat.add(record.getFatSnapshot().multiply(ratio));
            carbs = carbs.add(record.getCarbsSnapshot().multiply(ratio));
        }
        return new Totals(calories, protein, fat, carbs);
    }

    private DashboardTodayResponse.NutritionSummary nutrition(Totals totals, int calorieGoal) {
        // Default macro split: carbohydrates 50%, protein 20%, fat 30% of the calorie goal.
        BigDecimal carbsGoal = BigDecimal.valueOf(calorieGoal).multiply(new BigDecimal("0.50"))
                .divide(BigDecimal.valueOf(4), 1, RoundingMode.HALF_UP);
        BigDecimal proteinGoal = BigDecimal.valueOf(calorieGoal).multiply(new BigDecimal("0.20"))
                .divide(BigDecimal.valueOf(4), 1, RoundingMode.HALF_UP);
        BigDecimal fatGoal = BigDecimal.valueOf(calorieGoal).multiply(new BigDecimal("0.30"))
                .divide(BigDecimal.valueOf(9), 1, RoundingMode.HALF_UP);
        return new DashboardTodayResponse.NutritionSummary(
                metric(totals.carbs(), carbsGoal),
                metric(totals.protein(), proteinGoal),
                metric(totals.fat(), fatGoal)
        );
    }

    private DashboardTodayResponse.NutrientMetric metric(BigDecimal amount, BigDecimal goal) {
        int percent = goal.signum() == 0 ? 0 : amount.multiply(BigDecimal.valueOf(100))
                .divide(goal, 0, RoundingMode.HALF_UP).intValue();
        return new DashboardTodayResponse.NutrientMetric(display(amount), display(goal), Math.max(percent, 0));
    }

    private List<DashboardTodayResponse.MealSummary> mealSummaries(List<MealRecord> records) {
        Map<MealRecord.MealType, List<MealRecord>> groups = new EnumMap<>(MealRecord.MealType.class);
        for (MealRecord.MealType type : MealRecord.MealType.values()) groups.put(type, new ArrayList<>());
        for (MealRecord record : records) groups.get(record.getMealType()).add(record);

        List<DashboardTodayResponse.MealSummary> result = new ArrayList<>();
        for (MealRecord.MealType type : MealRecord.MealType.values()) {
            List<MealRecord> items = groups.get(type);
            BigDecimal calories = aggregate(items).calories();
            List<String> names = items.stream().map(MealRecord::getFoodNameSnapshot).distinct().limit(2).toList();
            result.add(new DashboardTodayResponse.MealSummary(
                    type.name(), mealLabel(type), items.size(), display(calories), names));
        }
        return result;
    }

    private String mealLabel(MealRecord.MealType type) {
        return switch (type) {
            case breakfast -> "早餐";
            case lunch -> "午餐";
            case dinner -> "晚餐";
            case snack -> "加餐";
        };
    }

    private DashboardTodayResponse.Advice advice(boolean empty, BigDecimal exceeded, BigDecimal remaining) {
        if (empty) return new DashboardTodayResponse.Advice("记录第一餐", "从一项真实饮食开始了解今日摄入");
        if (exceeded.signum() > 0) return new DashboardTodayResponse.Advice("今日已超出目标", "不必额外节食，下一餐恢复日常节奏即可");
        if (remaining.compareTo(BigDecimal.valueOf(300)) <= 0) {
            return new DashboardTodayResponse.Advice("即将达到今日目标", "下一餐可优先选择清淡、高蛋白的食物");
        }
        return new DashboardTodayResponse.Advice("保持当前节奏", "继续完整记录今日饮食");
    }

    private BigDecimal display(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private record Totals(BigDecimal calories, BigDecimal protein, BigDecimal fat, BigDecimal carbs) {}
}
