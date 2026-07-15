package com.diettracker.controller;

import com.diettracker.entity.ExerciseRecord;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.User;
import com.diettracker.entity.UserGoal;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.repository.FoodFavoriteRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserGoalRepository;
import com.diettracker.repository.UserRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "API_PERFORMANCE_TEST", matches = "true")
class ApiPerformanceTest {
    private static final int WARM_UP_RUNS = 5;
    private static final int MEASURED_RUNS = 40;

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository userRepository;
    @Autowired UserGoalRepository userGoalRepository;
    @Autowired FoodFavoriteRepository favoriteRepository;
    @Autowired FoodItemRepository foodItemRepository;
    @Autowired MealRecordRepository mealRecordRepository;
    @Autowired ExerciseRecordRepository exerciseRecordRepository;

    private final ZoneId zoneId = ZoneId.of("Asia/Shanghai");
    private LocalDate today;
    private String authorization;

    @BeforeEach
    void setUp() {
        exerciseRecordRepository.deleteAll();
        mealRecordRepository.deleteAll();
        favoriteRepository.deleteAll();
        foodItemRepository.deleteAll();
        userGoalRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setOpenid("performance-user");
        user.setNickname("performance-user");
        user.setDailyCalorieGoal(1800);
        userRepository.save(user);

        UserGoal goal = new UserGoal();
        goal.setUserId(user.getOpenid());
        goal.setCustomized(true);
        userGoalRepository.save(goal);

        FoodItem chicken = foodItemRepository.save(food("鸡胸肉"));
        List<FoodItem> searchableFoods = new ArrayList<>();
        for (int index = 0; index < 200; index++) {
            searchableFoods.add(food("性能验收食物-" + index));
        }
        foodItemRepository.saveAll(searchableFoods);

        today = LocalDate.now(zoneId);
        for (int offset = 0; offset < 90; offset++) {
            LocalDate date = today.minusDays(offset);
            mealRecordRepository.save(meal(chicken, date));
            exerciseRecordRepository.save(exercise(date));
        }
        authorization = "Bearer " + jwtUtil.generateToken(user.getOpenid());
    }

    @Test
    void criticalReadApisMeetPrdP95TargetsOnMySql8() throws Exception {
        long dashboardP95 = measure(get("/api/dashboard/today").param("date", today.toString()));
        long searchP95 = measure(get("/api/foods/search").param("keyword", "性能").param("size", "20"));
        long calendarP95 = measure(get("/api/calendar/summary").param("month", YearMonth.from(today).toString()));
        long trendP95 = measure(get("/api/stats/trend").param("range", "90d"));

        System.out.printf(
                "API performance baseline (MySQL 8, MockMvc, p95 ms): dashboard=%d search=%d calendar=%d trend=%d%n",
                dashboardP95, searchP95, calendarP95, trendP95);

        assertThat(dashboardP95).as("dashboard P95 milliseconds").isLessThanOrEqualTo(800);
        assertThat(searchP95).as("food search P95 milliseconds").isLessThanOrEqualTo(500);
        assertThat(calendarP95).as("calendar P95 milliseconds").isLessThanOrEqualTo(1200);
        assertThat(trendP95).as("90-day trend P95 milliseconds").isLessThanOrEqualTo(1200);
    }

    private long measure(MockHttpServletRequestBuilder request) throws Exception {
        for (int run = 0; run < WARM_UP_RUNS; run++) {
            perform(request);
        }
        List<Long> durations = new ArrayList<>(MEASURED_RUNS);
        for (int run = 0; run < MEASURED_RUNS; run++) {
            long startedAt = System.nanoTime();
            perform(request);
            durations.add((System.nanoTime() - startedAt) / 1_000_000);
        }
        Collections.sort(durations);
        return durations.get((int) Math.ceil(durations.size() * 0.95) - 1);
    }

    private void perform(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.header("Authorization", authorization))
                .andExpect(status().isOk());
    }

    private FoodItem food(String name) {
        FoodItem item = new FoodItem();
        item.setName(name);
        item.setBaseAmount(new BigDecimal("100"));
        item.setBaseUnit("g");
        item.setUnit("g");
        item.setCalories(new BigDecimal("165"));
        item.setProtein(new BigDecimal("31"));
        item.setFat(new BigDecimal("3.6"));
        item.setCarbs(BigDecimal.ZERO);
        item.setSource("SYSTEM");
        return item;
    }

    private MealRecord meal(FoodItem food, LocalDate date) {
        MealRecord record = new MealRecord();
        record.setUserId("performance-user");
        record.setMealDate(date);
        record.setMealType(MealRecord.MealType.lunch);
        record.setFoodItem(food);
        record.setQuantity(new BigDecimal("150"));
        record.setUnit("g");
        record.setFoodNameSnapshot(food.getName());
        record.setBaseAmountSnapshot(food.getBaseAmount());
        record.setBaseUnitSnapshot(food.getBaseUnit());
        record.setCaloriesSnapshot(food.getCalories());
        record.setProteinSnapshot(food.getProtein());
        record.setFatSnapshot(food.getFat());
        record.setCarbsSnapshot(food.getCarbs());
        return record;
    }

    private ExerciseRecord exercise(LocalDate date) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId("performance-user");
        record.setExerciseDate(date);
        record.setExerciseType(ExerciseRecord.ExerciseType.walking);
        record.setStartTime(LocalTime.of(18, 0));
        record.setDurationMinutes(20);
        record.setIntensity(ExerciseRecord.Intensity.medium);
        record.setCaloriesBurned(new BigDecimal("80"));
        record.setSource(ExerciseRecord.Source.MANUAL);
        return record;
    }
}
