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
import org.springframework.boot.test.mock.mockito.SpyBean;
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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CalendarApiTest.FixedClockConfig.class)
class CalendarApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired FoodItemRepository foodItemRepository;
    @SpyBean MealRecordRepository mealRecordRepository;
    @SpyBean ExerciseRecordRepository exerciseRecordRepository;
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
        clearInvocations(mealRecordRepository, exerciseRecordRepository);
    }

    @Test
    void returnsWholeMonthWithTwoAggregateQueriesAndReconcilesWithDashboard() throws Exception {
        LocalDate recordedDate = LocalDate.of(2026, 7, 13);
        mealRecordRepository.save(meal("user-a", recordedDate, MealRecord.MealType.breakfast, "400"));
        mealRecordRepository.save(meal("user-a", recordedDate, MealRecord.MealType.lunch, "840"));
        mealRecordRepository.save(meal("user-b", recordedDate, MealRecord.MealType.dinner, "9000"));
        exerciseRecordRepository.save(exercise("user-a", recordedDate, "380"));
        exerciseRecordRepository.save(exercise("user-b", recordedDate, "5000"));
        clearInvocations(mealRecordRepository, exerciseRecordRepository);

        mockMvc.perform(get("/api/calendar/summary")
                        .header("Authorization", bearer(token))
                        .param("month", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-07"))
                .andExpect(jsonPath("$.goalCalories").value(1800))
                .andExpect(jsonPath("$.days.length()").value(31))
                .andExpect(jsonPath("$.days[0].date").value("2026-07-01"))
                .andExpect(jsonPath("$.days[0].hasRecord").value(false))
                .andExpect(jsonPath("$.days[12].date").value("2026-07-13"))
                .andExpect(jsonPath("$.days[12].intakeCalories").value(1240))
                .andExpect(jsonPath("$.days[12].exerciseCalories").value(380))
                .andExpect(jsonPath("$.days[12].remainingCalories").value(560))
                .andExpect(jsonPath("$.days[12].mealCount").value(2))
                .andExpect(jsonPath("$.days[12].hasRecord").value(true));

        verify(mealRecordRepository, times(1)).summarizeCalendarByDate(
                "user-a", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        verify(exerciseRecordRepository, times(1)).summarizeCalendarByDate(
                "user-a", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        mockMvc.perform(get("/api/dashboard/today")
                        .header("Authorization", bearer(token))
                        .param("date", "2026-07-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeCalories").value(1240))
                .andExpect(jsonPath("$.exerciseCalories").value(380))
                .andExpect(jsonPath("$.remainingCalories").value(560));
    }

    @Test
    void validatesStrictMonthFormatAndRecentTwelveMonthWindow() throws Exception {
        mockMvc.perform(get("/api/calendar/summary")
                        .header("Authorization", bearer(token)).param("month", "2026-7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MONTH"));

        mockMvc.perform(get("/api/calendar/summary")
                        .header("Authorization", bearer(token)).param("month", "2025-07"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONTH_OUT_OF_RANGE"));

        mockMvc.perform(get("/api/calendar/summary")
                        .header("Authorization", bearer(token)).param("month", "2026-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONTH_OUT_OF_RANGE"));

        mockMvc.perform(get("/api/calendar/summary")
                        .header("Authorization", bearer(token)).param("month", "2025-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days.length()").value(31));
    }

    private User user(String id, Integer goal) {
        User user = new User();
        user.setOpenid(id);
        user.setNickname(id);
        user.setDailyCalorieGoal(goal);
        return user;
    }

    private FoodItem food() {
        FoodItem item = new FoodItem();
        item.setName("日历测试食物");
        item.setBaseAmount(new BigDecimal("100"));
        item.setBaseUnit("g");
        item.setUnit("g");
        item.setCalories(new BigDecimal("100"));
        item.setProtein(new BigDecimal("10"));
        item.setFat(new BigDecimal("5"));
        item.setCarbs(new BigDecimal("20"));
        return item;
    }

    private MealRecord meal(String userId, LocalDate date, MealRecord.MealType type, String calories) {
        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setMealDate(date);
        record.setMealType(type);
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
            return Clock.fixed(Instant.parse("2026-07-14T16:30:00Z"), ZoneId.of("Asia/Shanghai"));
        }
    }
}
