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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FoodDiscoveryApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired FoodFavoriteRepository favoriteRepository;
    @Autowired MealRecordRepository mealRecordRepository;
    @Autowired FoodItemRepository foodRepository;
    @Autowired FoodCategoryRepository categoryRepository;

    private String tokenA;
    private FoodItem chicken;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        mealRecordRepository.deleteAll();
        foodRepository.deleteAll();
        categoryRepository.deleteAll();

        FoodCategory category = new FoodCategory();
        category.setName("肉类");
        category.setSortOrder(1);
        category = categoryRepository.save(category);

        chicken = foodRepository.save(food("鸡胸肉", null, category, "165"));
        foodRepository.save(food("私房鸡肉卷", "user-a", category, "210"));
        foodRepository.save(food("私房鸡肉饭", "user-b", category, "240"));
        tokenA = jwtUtil.generateToken("user-a");
    }

    @Test
    void searchesVisibleFoodsWithPaginationAndNoOwnershipLeak() throws Exception {
        mockMvc.perform(get("/api/foods/search")
                        .header("Authorization", bearer(tokenA))
                        .param("keyword", " 鸡 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0]", not(hasKey("userId"))))
                .andExpect(jsonPath("$.items[0].baseAmount").value(100))
                .andExpect(jsonPath("$.items[0].baseUnit").value("g"));
    }

    @Test
    void rejectsBlankSearchKeyword() throws Exception {
        mockMvc.perform(get("/api/foods/search")
                        .header("Authorization", bearer(tokenA))
                        .param("keyword", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_KEYWORD"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void favoritesAndListsOnlyAuthenticatedUsersChoice() throws Exception {
        mockMvc.perform(put("/api/foods/{id}/favorite", chicken.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"favorite\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", bearer(tokenA))
                        .param("scope", "favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("鸡胸肉"));
    }

    @Test
    void commonAndRecentAreDerivedFromCurrentUsersRecords() throws Exception {
        MealRecord record = new MealRecord();
        record.setMealDate(LocalDate.now());
        record.setMealType(MealRecord.MealType.lunch);
        record.setFoodItem(chicken);
        record.setQuantity(new BigDecimal("150"));
        record.setUnit("g");
        record.setUserId("user-a");
        record.setRecordTime(LocalDateTime.now());
        mealRecordRepository.save(record);

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", bearer(tokenA))
                        .param("scope", "common"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("鸡胸肉"));
    }

    @Test
    void createsCustomFoodWithExplicitBasisAndKeepsItPrivate() throws Exception {
        mockMvc.perform(post("/api/foods")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"低脂鸡肉卷","baseAmount":100,"baseUnit":"g","calories":180}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseAmount").value(100))
                .andExpect(jsonPath("$.baseUnit").value("g"))
                .andExpect(jsonPath("$.source").value("USER_CUSTOM"))
                .andExpect(jsonPath("$.custom").value(true));

        String tokenB = jwtUtil.generateToken("user-b");
        mockMvc.perform(get("/api/foods/search")
                        .header("Authorization", bearer(tokenB))
                        .param("keyword", "低脂鸡肉卷"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void returnsFoodDetailAndServerVerifiedNutritionPreview() throws Exception {
        mockMvc.perform(get("/api/foods/{id}", chicken.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("鸡胸肉"))
                .andExpect(jsonPath("$.baseAmount").value(100));

        mockMvc.perform(post("/api/foods/{id}/calculate", chicken.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150))
                .andExpect(jsonPath("$.unit").value("g"))
                .andExpect(jsonPath("$.calories").value(248));
    }

    @Test
    void returnsFieldErrorForInvalidCalculationAmount() throws Exception {
        mockMvc.perform(post("/api/foods/{id}/calculate", chicken.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1.11}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.amount").value("食用重量最多保留 1 位小数"));
    }

    private FoodItem food(String name, String userId, FoodCategory category, String calories) {
        FoodItem food = new FoodItem();
        food.setName(name);
        food.setCategory(category);
        food.setCalories(new BigDecimal(calories));
        food.setProtein(BigDecimal.ZERO);
        food.setFat(BigDecimal.ZERO);
        food.setCarbs(BigDecimal.ZERO);
        food.setBaseAmount(new BigDecimal("100"));
        food.setBaseUnit("g");
        food.setUnit("g");
        food.setSource(userId == null ? "SYSTEM" : "USER_CUSTOM");
        food.setUserId(userId);
        return food;
    }

    private String bearer(String token) { return "Bearer " + token; }
}
