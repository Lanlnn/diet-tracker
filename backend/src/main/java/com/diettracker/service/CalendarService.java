package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.CalendarSummaryResponse;
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
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final MealRecordRepository mealRecordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;

    public CalendarService(MealRecordRepository mealRecordRepository,
                           ExerciseRecordRepository exerciseRecordRepository,
                           UserRepository userRepository,
                           UserGoalRepository userGoalRepository) {
        this.mealRecordRepository = mealRecordRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.userRepository = userRepository;
        this.userGoalRepository = userGoalRepository;
    }

    @Transactional(readOnly = true)
    public CalendarSummaryResponse getSummary(String userId, String monthValue, LocalDate today) {
        YearMonth month = parseMonth(monthValue);
        YearMonth currentMonth = YearMonth.from(today);
        if (month.isBefore(currentMonth.minusMonths(11)) || month.isAfter(currentMonth)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MONTH_OUT_OF_RANGE", "month 仅支持最近 12 个月");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
        var userGoal = userGoalRepository.findById(userId).orElse(null);
        Integer savedGoal = userGoal == null ? null : userGoal.getDailyCalorieGoal();
        int goalCalories = savedGoal == null ? DashboardService.DEFAULT_CALORIE_GOAL : savedGoal;
        String goalSource = userGoal == null || !userGoal.isCustomized() ? "DEFAULT" : "USER";
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        Map<LocalDate, MealAggregate> meals = mealMap(
                mealRecordRepository.summarizeCalendarByDate(userId, start, end));
        Map<LocalDate, BigDecimal> exercises = exerciseMap(
                exerciseRecordRepository.summarizeCalendarByDate(userId, start, end));

        List<CalendarSummaryResponse.CalendarDay> days = new ArrayList<>(month.lengthOfMonth());
        BigDecimal goal = BigDecimal.valueOf(goalCalories);
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            MealAggregate meal = meals.getOrDefault(date, new MealAggregate(ZERO, 0));
            BigDecimal exercise = exercises.getOrDefault(date, ZERO);
            days.add(new CalendarSummaryResponse.CalendarDay(
                    date,
                    display(meal.intake()),
                    display(exercise),
                    display(goal.subtract(meal.intake()).max(ZERO)),
                    meal.mealCount(),
                    meals.containsKey(date) || exercises.containsKey(date)
            ));
        }
        return new CalendarSummaryResponse(month, goalCalories, goalSource, days);
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MONTH", "month 必须为 yyyy-MM 格式");
        }
    }

    private Map<LocalDate, MealAggregate> mealMap(List<Object[]> rows) {
        Map<LocalDate, MealAggregate> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put((LocalDate) row[0], new MealAggregate(decimal(row[1]), ((Number) row[2]).intValue()));
        }
        return values;
    }

    private Map<LocalDate, BigDecimal> exerciseMap(List<Object[]> rows) {
        Map<LocalDate, BigDecimal> values = new HashMap<>();
        for (Object[] row : rows) values.put((LocalDate) row[0], decimal(row[1]));
        return values;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return ZERO;
    }

    private BigDecimal display(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private record MealAggregate(BigDecimal intake, int mealCount) {}
}
