package com.diettracker.controller;

import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.User;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired FoodItemRepository foodItemRepository;
    @Autowired MealRecordRepository mealRecordRepository;
    @Autowired JwtUtil jwtUtil;

    private final LocalDate date = LocalDate.of(2026, 7, 15);
    private String tokenA;
    private FoodItem chicken;

    @BeforeEach
    void setUp() {
        mealRecordRepository.deleteAll();
        foodItemRepository.deleteAll();
        userRepository.deleteAll();

        User userA = user("user-a", 1800);
        User userB = user("user-b", 1800);
        userRepository.save(userA);
        userRepository.save(userB);
        chicken = foodItemRepository.save(food("鸡胸肉"));
        tokenA = jwtUtil.generateToken("user-a");
    }

    @Test
    void returnsOneConsistentUserIsolatedDashboardSnapshot() throws Exception {
        mealRecordRepository.save(record("user-a", new BigDecimal("150"), MealRecord.MealType.lunch));
        mealRecordRepository.save(record("user-b", new BigDecimal("900"), MealRecord.MealType.dinner));

        mockMvc.perform(get("/api/dashboard/today")
                        .header("Authorization", bearer(tokenA))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-07-15"))
                .andExpect(jsonPath("$.goalCalories").value(1800))
                .andExpect(jsonPath("$.goalSource").value("USER"))
                .andExpect(jsonPath("$.intakeCalories").value(247.5))
                .andExpect(jsonPath("$.remainingCalories").value(1552.5))
                .andExpect(jsonPath("$.exceededCalories").value(0))
                .andExpect(jsonPath("$.exerciseCalories").value(0))
                .andExpect(jsonPath("$.netCalories").value(247.5))
                .andExpect(jsonPath("$.nutrition.protein.amount").value(46.5))
                .andExpect(jsonPath("$.meals[1].type").value("lunch"))
                .andExpect(jsonPath("$.meals[1].itemCount").value(1))
                .andExpect(jsonPath("$.meals[1].calories").value(247.5))
                .andExpect(jsonPath("$.exercise.state").value("empty"));

        mockMvc.perform(get("/api/records/stats/daily")
                        .header("Authorization", bearer(tokenA))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(247.5));
    }

    @Test
    void clampsRemainingAtZeroAndReturnsExceededAmount() throws Exception {
        mealRecordRepository.save(record("user-a", new BigDecimal("1200"), MealRecord.MealType.dinner));

        mockMvc.perform(get("/api/dashboard/today")
                        .header("Authorization", bearer(tokenA))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeCalories").value(1980))
                .andExpect(jsonPath("$.remainingCalories").value(0))
                .andExpect(jsonPath("$.exceededCalories").value(180))
                .andExpect(jsonPath("$.exerciseCalories").value(0))
                .andExpect(jsonPath("$.advice.title").value("今日已超出目标"));
    }

    @Test
    void returnsDedicatedEmptyStateAndDefaultGoal() throws Exception {
        User user = userRepository.findById("user-a").orElseThrow();
        user.setDailyCalorieGoal(null);
        userRepository.save(user);

        mockMvc.perform(get("/api/dashboard/today")
                        .header("Authorization", bearer(tokenA))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalCalories").value(1800))
                .andExpect(jsonPath("$.goalSource").value("DEFAULT"))
                .andExpect(jsonPath("$.intakeCalories").value(0))
                .andExpect(jsonPath("$.meals.length()").value(4))
                .andExpect(jsonPath("$.advice.title").value("记录第一餐"));
    }

    private User user(String id, Integer goal) {
        User user = new User();
        user.setOpenid(id);
        user.setNickname(id);
        user.setDailyCalorieGoal(goal);
        return user;
    }

    private FoodItem food(String name) {
        FoodItem food = new FoodItem();
        food.setName(name);
        food.setBaseAmount(new BigDecimal("100"));
        food.setBaseUnit("g");
        food.setUnit("g");
        food.setCalories(new BigDecimal("165"));
        food.setProtein(new BigDecimal("31"));
        food.setFat(new BigDecimal("3.6"));
        food.setCarbs(BigDecimal.ZERO);
        return food;
    }

    private MealRecord record(String userId, BigDecimal quantity, MealRecord.MealType type) {
        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setMealDate(date);
        record.setMealType(type);
        record.setFoodItem(chicken);
        record.setQuantity(quantity);
        record.setUnit("g");
        record.setFoodNameSnapshot(chicken.getName());
        record.setBaseAmountSnapshot(chicken.getBaseAmount());
        record.setBaseUnitSnapshot(chicken.getBaseUnit());
        record.setCaloriesSnapshot(chicken.getCalories());
        record.setProteinSnapshot(chicken.getProtein());
        record.setFatSnapshot(chicken.getFat());
        record.setCarbsSnapshot(chicken.getCarbs());
        return record;
    }

    private String bearer(String token) { return "Bearer " + token; }
}
