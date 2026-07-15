package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.TrendResponse;
import com.diettracker.entity.User;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserRepository;
import com.diettracker.repository.UserGoalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TrendService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final MealRecordRepository mealRecordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    public TrendService(MealRecordRepository mealRecordRepository,
                        ExerciseRecordRepository exerciseRecordRepository,
                        UserRepository userRepository,
                        UserGoalRepository userGoalRepository) {
        this.mealRecordRepository = mealRecordRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.userRepository = userRepository;
        this.userGoalRepository = userGoalRepository;
    }

    @Transactional(readOnly = true)
    public TrendResponse getTrend(String userId, String range, LocalDate endDate) {
        int days = parseDays(range);
        LocalDate startDate = endDate.minusDays(days - 1L);
        LocalDate comparisonStart = startDate.minusDays(days);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
        int calorieGoal = userGoalRepository.findById(userId)
                .map(goal -> goal.getDailyCalorieGoal())
                .orElse(DashboardService.DEFAULT_CALORIE_GOAL);

        Map<LocalDate, BigDecimal> intake = toDailyMap(
                mealRecordRepository.sumCaloriesGroupByDate(userId, comparisonStart, endDate));
        Map<LocalDate, BigDecimal> exercise = toDailyMap(
                exerciseRecordRepository.sumCaloriesGroupByDate(userId, comparisonStart, endDate));

        List<TrendResponse.TrendDay> dailyData = new ArrayList<>(days);
        Set<LocalDate> recordedDates = new HashSet<>();
        BigDecimal totalIntake = ZERO;
        BigDecimal totalExercise = ZERO;
        int achievedDays = 0;
        BigDecimal lowerGoal = BigDecimal.valueOf(calorieGoal).multiply(new BigDecimal("0.80"));
        BigDecimal upperGoal = BigDecimal.valueOf(calorieGoal).multiply(new BigDecimal("1.20"));

        for (int offset = 0; offset < days; offset++) {
            LocalDate date = startDate.plusDays(offset);
            BigDecimal dailyIntake = intake.getOrDefault(date, ZERO);
            BigDecimal dailyExercise = exercise.getOrDefault(date, ZERO);
            boolean hasData = intake.containsKey(date) || exercise.containsKey(date);
            if (hasData) recordedDates.add(date);
            if (intake.containsKey(date)
                    && dailyIntake.compareTo(lowerGoal) >= 0
                    && dailyIntake.compareTo(upperGoal) <= 0) achievedDays++;
            totalIntake = totalIntake.add(dailyIntake);
            totalExercise = totalExercise.add(dailyExercise);
            dailyData.add(new TrendResponse.TrendDay(
                    date, display(dailyIntake), display(dailyExercise),
                    display(dailyIntake.subtract(dailyExercise)), hasData));
        }

        BigDecimal averageIntake = average(totalIntake, days);
        BigDecimal averageExercise = average(totalExercise, days);
        BigDecimal averageNet = averageIntake.subtract(averageExercise);
        BigDecimal previousNet = averageNet(intake, exercise, comparisonStart, days);
        Integer changePercent = percentChange(averageNet, previousNet);
        int achievementRate = recordedDates.isEmpty() ? 0
                : BigDecimal.valueOf(achievedDays * 100L)
                .divide(BigDecimal.valueOf(recordedDates.size()), 0, RoundingMode.HALF_UP).intValue();
        List<TrendResponse.TrendSummary> summaries = summaries(
                recordedDates.size(), averageNet, averageExercise, calorieGoal, changePercent);

        return new TrendResponse(
                range, startDate, endDate, calorieGoal, recordedDates.size(),
                display(averageIntake), display(averageExercise), display(averageNet),
                changePercent, achievementRate, accessibilitySummary(dailyData),
                dailyData, summaries);
    }

    private int parseDays(String range) {
        return switch (range) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RANGE", "range 仅支持 7d、30d 或 90d");
        };
    }

    private Map<LocalDate, BigDecimal> toDailyMap(List<Object[]> rows) {
        Map<LocalDate, BigDecimal> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put((LocalDate) row[0], decimal(row[1]));
        }
        return values;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return ZERO;
    }

    private BigDecimal average(BigDecimal total, int days) {
        return total.divide(BigDecimal.valueOf(days), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal averageNet(Map<LocalDate, BigDecimal> intake,
                                  Map<LocalDate, BigDecimal> exercise,
                                  LocalDate startDate,
                                  int days) {
        BigDecimal total = ZERO;
        for (int offset = 0; offset < days; offset++) {
            LocalDate date = startDate.plusDays(offset);
            total = total.add(intake.getOrDefault(date, ZERO))
                    .subtract(exercise.getOrDefault(date, ZERO));
        }
        return average(total, days);
    }

    private Integer percentChange(BigDecimal current, BigDecimal previous) {
        if (previous.abs().compareTo(new BigDecimal("0.1")) < 0) return null;
        return current.subtract(previous).multiply(BigDecimal.valueOf(100))
                .divide(previous.abs(), 0, RoundingMode.HALF_UP).intValue();
    }

    private List<TrendResponse.TrendSummary> summaries(int recordedDays,
                                                        BigDecimal averageNet,
                                                        BigDecimal averageExercise,
                                                        int calorieGoal,
                                                        Integer changePercent) {
        if (recordedDays < 3) {
            return List.of(new TrendResponse.TrendSummary(
                    "continue", "继续记录", "至少记录 3 天后，才能生成可靠的趋势小结。"));
        }

        List<TrendResponse.TrendSummary> result = new ArrayList<>(2);
        BigDecimal goal = BigDecimal.valueOf(calorieGoal);
        if (averageNet.compareTo(goal.multiply(new BigDecimal("1.10"))) > 0) {
            result.add(new TrendResponse.TrendSummary(
                    "intake-high", "净摄入偏高", "近期日均净摄入高于目标，可先检查份量与加餐记录。"));
        } else if (averageNet.compareTo(goal.multiply(new BigDecimal("0.70"))) < 0) {
            result.add(new TrendResponse.TrendSummary(
                    "intake-low", "净摄入偏低", "近期日均净摄入较低，请保持规律饮食，不建议额外节食。"));
        } else {
            result.add(new TrendResponse.TrendSummary(
                    "intake-steady", "热量控制稳定", "近期净摄入处于目标区间，继续保持完整记录。"));
        }

        if (averageExercise.signum() == 0) {
            result.add(new TrendResponse.TrendSummary(
                    "exercise-none", "增加日常活动", "本周期还没有运动消耗记录，可从轻量活动开始。"));
        } else if (changePercent != null && Math.abs(changePercent) >= 10) {
            String direction = changePercent > 0 ? "上升" : "下降";
            result.add(new TrendResponse.TrendSummary(
                    "trend-change", "关注周期变化", "日均净摄入较上一周期" + direction + Math.abs(changePercent) + "%。"));
        } else {
            result.add(new TrendResponse.TrendSummary(
                    "exercise-steady", "运动习惯在积累", "本周期已有稳定的运动消耗记录，按身体感受保持节奏。"));
        }
        return result;
    }

    private String accessibilitySummary(List<TrendResponse.TrendDay> dailyData) {
        TrendResponse.TrendDay highest = dailyData.stream()
                .max((left, right) -> left.netCalories().compareTo(right.netCalories()))
                .orElseThrow();
        TrendResponse.TrendDay lowest = dailyData.stream()
                .min((left, right) -> left.netCalories().compareTo(right.netCalories()))
                .orElseThrow();
        return "趋势共 " + dailyData.size() + " 天；净摄入最高为 " + highest.date() + " 的 "
                + highest.netCalories().toPlainString() + " 千卡，最低为 " + lowest.date() + " 的 "
                + lowest.netCalories().toPlainString() + " 千卡。";
    }

    private BigDecimal display(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
