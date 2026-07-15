package com.diettracker.controller;

import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodFavoriteRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MealRecordApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired MealRecordRepository recordRepository;
    @Autowired FoodFavoriteRepository favoriteRepository;
    @Autowired FoodItemRepository foodRepository;
    @Autowired FoodCategoryRepository categoryRepository;

    private FoodItem chicken;
    private String tokenA;
    private String tokenB;
    private final LocalDate date = LocalDate.of(2026, 7, 15);

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        recordRepository.deleteAll();
        foodRepository.deleteAll();
        categoryRepository.deleteAll();
        FoodCategory category = new FoodCategory();
        category.setName("肉类");
        category.setSortOrder(1);
        category = categoryRepository.save(category);
        chicken = new FoodItem();
        chicken.setName("鸡胸肉");
        chicken.setCategory(category);
        chicken.setBaseAmount(new BigDecimal("100"));
        chicken.setBaseUnit("g");
        chicken.setUnit("g");
        chicken.setCalories(new BigDecimal("165"));
        chicken.setProtein(new BigDecimal("31"));
        chicken.setFat(new BigDecimal("3.6"));
        chicken.setCarbs(BigDecimal.ZERO);
        chicken = foodRepository.save(chicken);
        tokenA = jwtUtil.generateToken("user-a");
        tokenB = jwtUtil.generateToken("user-b");
    }

    @Test
    void createRetryUpdateAndDeleteKeepSnapshotAggregateConsistent() throws Exception {
        String body = """
                {"mealDate":"2026-07-15","mealType":"lunch","foodItemId":%d,"quantity":150,"unit":"g"}
                """.formatted(chicken.getId());
        String first = mockMvc.perform(post("/api/records")
                        .header("Authorization", bearer(tokenA))
                        .header("X-Idempotency-Key", "meal-request-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodNameSnapshot").value("鸡胸肉"))
                .andExpect(jsonPath("$.caloriesSnapshot").value(165))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/records")
                        .header("Authorization", bearer(tokenA))
                        .header("X-Idempotency-Key", "meal-request-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        assertThat(recordRepository.count()).isEqualTo(1);

        MealRecord record = recordRepository.findByUserIdAndMealDateOrderByRecordTimeAsc("user-a", date).get(0);
        chicken.setCalories(new BigDecimal("999"));
        chicken.setProtein(BigDecimal.ZERO);
        foodRepository.save(chicken);

        mockMvc.perform(get("/api/records/stats/daily")
                        .header("Authorization", bearer(tokenA)).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(247.5))
                .andExpect(jsonPath("$.totalProtein").value(46.5));

        mockMvc.perform(put("/api/records/{id}", record.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealType\":\"dinner\",\"quantity\":200,\"unit\":\"g\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealType").value("dinner"))
                .andExpect(jsonPath("$.quantity").value(200));

        mockMvc.perform(delete("/api/records/{id}", record.getId())
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(delete("/api/records/{id}", record.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/records/{id}", record.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECORD_NOT_FOUND"));
    }

    private String bearer(String token) { return "Bearer " + token; }
}
