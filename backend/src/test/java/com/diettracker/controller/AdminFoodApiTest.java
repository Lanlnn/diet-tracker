package com.diettracker.controller;

import com.diettracker.admin.AdminRole;
import com.diettracker.entity.AdminUser;
import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.repository.*;
import com.diettracker.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFoodApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminUserRepository adminUsers;
    @Autowired AdminAuditLogRepository auditLogs;
    @Autowired FoodCategoryRepository categories;
    @Autowired FoodItemRepository foods;
    @Autowired MealRecordRepository records;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private FoodItem chicken;
    private MealRecord snapshot;

    @BeforeEach
    void setUp() {
        auditLogs.deleteAll(); adminUsers.deleteAll(); records.deleteAll(); foods.deleteAll(); categories.deleteAll();
        createAdmin("editor", AdminRole.FOOD_EDITOR);
        createAdmin("viewer", AdminRole.SUPPORT_VIEWER);

        FoodCategory category = new FoodCategory(); category.setName("肉蛋水产"); category.setIcon("protein"); category.setSortOrder(10);
        category = categories.save(category);
        chicken = new FoodItem(); chicken.setName("鸡胸肉"); chicken.setCategory(category); chicken.setBaseAmount(new BigDecimal("100"));
        chicken.setBaseUnit("g"); chicken.setUnit("g"); chicken.setCalories(new BigDecimal("165")); chicken.setProtein(new BigDecimal("31"));
        chicken.setFat(new BigDecimal("3.6")); chicken.setCarbs(BigDecimal.ZERO); chicken.setSource("SYSTEM_SEED"); chicken.setUserId(null);
        chicken = foods.save(chicken);

        snapshot = new MealRecord(); snapshot.setMealDate(LocalDate.of(2026, 7, 15)); snapshot.setMealType(MealRecord.MealType.lunch);
        snapshot.setFoodItem(chicken); snapshot.setQuantity(new BigDecimal("150")); snapshot.setUnit("g"); snapshot.setUserId("test-user");
        snapshot = records.save(snapshot);
    }

    @Test
    void separatesAdminAuthenticationAndEnforcesRole() throws Exception {
        mvc.perform(get("/api/admin/foods")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ADMIN_AUTH_REQUIRED"));

        String viewer = login("viewer");
        mvc.perform(put("/api/admin/foods/{id}", chicken.getId())
                        .header("Authorization", "Bearer " + viewer)
                        .header("X-Audit-Reason", "尝试越权")
                        .contentType(MediaType.APPLICATION_JSON).content(foodJson("180")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ADMIN_FORBIDDEN"));
    }

    @Test
    void previewsWithSharedCalculationAndKeepsHistoricalSnapshot() throws Exception {
        String token = login("editor");
        mvc.perform(post("/api/admin/foods/preview").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"food\":" + foodJson("165") + ",\"amount\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(248))
                .andExpect(jsonPath("$.protein").value(46.5));

        mvc.perform(put("/api/admin/foods/{id}", chicken.getId())
                        .header("Authorization", "Bearer " + token)
                        .header("X-Audit-Reason", "供应商复核后修正热量")
                        .contentType(MediaType.APPLICATION_JSON).content(foodJson("200")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.calories").value(200));

        MealRecord unchanged = records.findById(snapshot.getId()).orElseThrow();
        assertThat(unchanged.getCaloriesSnapshot()).isEqualByComparingTo("165");
        assertThat(auditLogs.count()).isEqualTo(1);
    }

    @Test
    void adminFoodUpdateFlowsThroughMiniappSearchAndCalculationWithoutRewritingHistory() throws Exception {
        String adminToken = login("editor");
        String miniappToken = jwtUtil.generateToken("test-user");

        mvc.perform(put("/api/admin/foods/{id}", chicken.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Audit-Reason", "跨端契约测试修正热量")
                        .contentType(MediaType.APPLICATION_JSON).content(foodJson("200")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/foods/search")
                        .header("Authorization", "Bearer " + miniappToken)
                        .param("keyword", "鸡胸肉")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(chicken.getId()))
                .andExpect(jsonPath("$.items[0].calories").value(200));

        mvc.perform(post("/api/foods/{id}/calculate", chicken.getId())
                        .header("Authorization", "Bearer " + miniappToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(300))
                .andExpect(jsonPath("$.protein").value(46.5));

        MealRecord unchanged = records.findById(snapshot.getId()).orElseThrow();
        assertThat(unchanged.getCaloriesSnapshot()).isEqualByComparingTo("165");
        assertThat(auditLogs.count()).isEqualTo(1);
    }

    @Test
    void logoutInvalidatesExistingToken() throws Exception {
        String token = login("editor");
        mvc.perform(post("/api/admin/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/admin/foods").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("ADMIN_TOKEN_INVALID"));
    }

    private void createAdmin(String username, AdminRole role) {
        AdminUser user = new AdminUser(); user.setUsername(username); user.setDisplayName(username); user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode("correct-password-123")); user.setEnabled(true); adminUsers.save(user);
    }

    private String login(String username) throws Exception {
        String response = mvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct-password-123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String foodJson(String calories) {
        return "{\"name\":\"鸡胸肉\",\"categoryId\":" + chicken.getCategory().getId()
                + ",\"baseAmount\":100,\"baseUnit\":\"g\",\"servingAmount\":null,\"servingUnit\":null"
                + ",\"calories\":" + calories + ",\"protein\":31,\"fat\":3.6,\"carbs\":0,\"source\":\"SYSTEM_EDITORIAL\"}";
    }
}
