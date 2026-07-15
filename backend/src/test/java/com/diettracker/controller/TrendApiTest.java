package com.diettracker.controller;

import com.diettracker.entity.ExerciseRecord;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.User;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TrendApiTest.FixedClockConfig.class)
class TrendApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired FoodItemRepository foodItemRepository;
    @Autowired MealRecordRepository mealRecordRepository;
    @Autowired ExerciseRecordRepository exerciseRecordRepository;
    @Autowired JwtUtil jwtUtil;

    private FoodItem food;
    private String token;

    @BeforeEach
    void setUp() {
        exerciseRecordRepository.deleteAll();
        mealRecordRepository.deleteAll();
        foodItemRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(user("user-a", 1800));
        userRepository.save(user("user-b", 1800));
        food = foodItemRepository.save(food());
        token = jwtUtil.generateToken("user-a");
    }

    @Test
    void returnsContinuousSnapshotBasedTrendAndReconcilesWithDashboard() throws Exception {
        mealRecordRepository.save(meal("user-a", LocalDate.of(2026, 7, 8), "700"));
        mealRecordRepository.save(meal("user-a", LocalDate.of(2026, 7, 13), "1800"));
        mealRecordRepository.save(meal("user-a", LocalDate.of(2026, 7, 14), "1000"));
        mealRecordRepository.save(meal("user-a", LocalDate.of(2026, 7, 15), "800"));
        mealRecordRepository.save(meal("user-b", LocalDate.of(2026, 7, 15), "9000"));
        exerciseRecordRepository.save(exercise("user-a", LocalDate.of(2026, 7, 13), "100"));
        exerciseRecordRepository.save(exercise("user-a", LocalDate.of(2026, 7, 14), "200"));
        exerciseRecordRepository.save(exercise("user-b", LocalDate.of(2026, 7, 15), "500"));

        mockMvc.perform(get("/api/stats/trend")
                        .header("Authorization", bearer(token))
                        .param("range", "7d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-07-09"))
                .andExpect(jsonPath("$.endDate").value("2026-07-15"))
                .andExpect(jsonPath("$.dailyData.length()").value(7))
                .andExpect(jsonPath("$.dailyData[0].date").value("2026-07-09"))
                .andExpect(jsonPath("$.dailyData[0].intakeCalories").value(0))
                .andExpect(jsonPath("$.dailyData[4].netCalories").value(1700))
                .andExpect(jsonPath("$.dailyData[6].intakeCalories").value(800))
                .andExpect(jsonPath("$.dailyData[6].netCalories").value(800))
                .andExpect(jsonPath("$.averageIntake").value(514.3))
                .andExpect(jsonPath("$.averageExercise").value(42.9))
                .andExpect(jsonPath("$.averageNetIntake").value(471.4))
                .andExpect(jsonPath("$.recordedDays").value(3))
                .andExpect(jsonPath("$.nutritionAchievementRate").value(33))
                .andExpect(jsonPath("$.summaries.length()").value(2));

        mockMvc.perform(get("/api/dashboard/today")
                        .header("Authorization", bearer(token))
                        .param("date", "2026-07-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeCalories").value(800))
                .andExpect(jsonPath("$.exerciseCalories").value(0))
                .andExpect(jsonPath("$.netCalories").value(800));
    }

    @Test
    void validatesRangeAndSuppressesConclusionsUntilThreeRecordedDays() throws Exception {
        mealRecordRepository.save(meal("user-a", LocalDate.of(2026, 7, 15), "500"));

        mockMvc.perform(get("/api/stats/trend")
                        .header("Authorization", bearer(token))
                        .param("range", "30d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyData.length()").value(30))
                .andExpect(jsonPath("$.recordedDays").value(1))
                .andExpect(jsonPath("$.summaries.length()").value(1))
                .andExpect(jsonPath("$.summaries[0].type").value("continue"));

        mockMvc.perform(get("/api/stats/trend")
                        .header("Authorization", bearer(token))
                        .param("range", "14d"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RANGE"));
    }

    private User user(String id, int goal) {
        User user = new User();
        user.setOpenid(id);
        user.setNickname(id);
        user.setDailyCalorieGoal(goal);
        return user;
    }

    private FoodItem food() {
        FoodItem item = new FoodItem();
        item.setName("趋势测试食物");
        item.setBaseAmount(new BigDecimal("100"));
        item.setBaseUnit("g");
        item.setUnit("g");
        item.setCalories(new BigDecimal("100"));
        item.setProtein(new BigDecimal("10"));
        item.setFat(new BigDecimal("5"));
        item.setCarbs(new BigDecimal("20"));
        return item;
    }

    private MealRecord meal(String userId, LocalDate date, String calories) {
        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setMealDate(date);
        record.setMealType(MealRecord.MealType.lunch);
        record.setFoodItem(food);
        record.setQuantity(new BigDecimal(calories));
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

    private ExerciseRecord exercise(String userId, LocalDate date, String calories) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setExerciseDate(date);
        record.setExerciseType(ExerciseRecord.ExerciseType.walking);
        record.setStartTime(LocalTime.of(18, 0));
        record.setDurationMinutes(20);
        record.setIntensity(ExerciseRecord.Intensity.medium);
        record.setCaloriesBurned(new BigDecimal(calories));
        record.setSource(ExerciseRecord.Source.MANUAL);
        return record;
    }

    private String bearer(String value) { return "Bearer " + value; }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            // UTC is still July 14; the configured Asia/Shanghai boundary resolves to July 15.
            return Clock.fixed(Instant.parse("2026-07-14T16:30:00Z"), ZoneId.of("Asia/Shanghai"));
        }
    }
}
