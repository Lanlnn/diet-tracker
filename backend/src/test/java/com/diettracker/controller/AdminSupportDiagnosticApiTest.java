package com.diettracker.controller;

import com.diettracker.admin.AdminRole;
import com.diettracker.entity.*;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSupportDiagnosticApiTest {
    private static final LocalDate DATE = LocalDate.of(2026, 7, 15);
    private static final String PRIVATE_OPENID = "openid-must-never-leave-admin-api";
    private static final String REASON = "核对用户反馈的今日摄入数据";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository users;
    @Autowired AdminUserRepository adminUsers;
    @Autowired AdminAuditLogRepository auditLogs;
    @Autowired MealRecordRepository meals;
    @Autowired ExerciseRecordRepository exercises;
    @Autowired FoodItemRepository foods;
    @Autowired FoodCategoryRepository categories;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private User user;
    private FoodItem food;

    @BeforeEach
    void setUp() {
        auditLogs.deleteAll(); adminUsers.deleteAll(); exercises.deleteAll(); meals.deleteAll();
        foods.deleteAll(); categories.deleteAll(); users.deleteAll();
        createAdmin("super", AdminRole.SUPER_ADMIN);
        createAdmin("support", AdminRole.SUPPORT_VIEWER);
        createAdmin("food-editor", AdminRole.FOOD_EDITOR);

        user = new User(); user.setOpenid(PRIVATE_OPENID); user.setNickname("诊断用户");
        user.setDailyCalorieGoal(2000); user = users.saveAndFlush(user);

        FoodCategory category = new FoodCategory(); category.setName("蛋白质"); category.setSortOrder(10);
        category = categories.save(category);
        food = new FoodItem(); food.setName("快照鸡胸肉"); food.setCategory(category);
        food.setBaseAmount(new BigDecimal("100")); food.setBaseUnit("g"); food.setUnit("g");
        food.setCalories(new BigDecimal("165")); food.setProtein(new BigDecimal("31"));
        food.setFat(new BigDecimal("3.6")); food.setCarbs(BigDecimal.ZERO);
        food.setSource("SYSTEM_EDITORIAL"); food = foods.save(food);

        MealRecord record = new MealRecord(); record.setMealDate(DATE); record.setMealType(MealRecord.MealType.lunch);
        record.setFoodItem(food); record.setQuantity(new BigDecimal("150")); record.setUnit("g");
        record.setUserId(PRIVATE_OPENID); record.setClientRequestId("meal-idempotency-001");
        record.setRecordTime(LocalDateTime.of(2026, 7, 15, 12, 30)); meals.save(record);

        ExerciseRecord exercise = new ExerciseRecord(); exercise.setUserId(PRIVATE_OPENID); exercise.setExerciseDate(DATE);
        exercise.setExerciseType(ExerciseRecord.ExerciseType.walking); exercise.setDurationMinutes(30);
        exercise.setIntensity(ExerciseRecord.Intensity.medium); exercise.setCaloriesBurned(new BigDecimal("120"));
        exercise.setSource(ExerciseRecord.Source.MANUAL); exercise.setStartTime(LocalTime.of(18, 0)); exercises.save(exercise);
    }

    @Test
    void adminTodayExactlyMatchesMiniappDashboardForSameUserAndDate() throws Exception {
        String miniapp = mvc.perform(get("/api/dashboard/today")
                        .header("Authorization", "Bearer " + jwtUtil.generateToken(PRIVATE_OPENID))
                        .param("date", DATE.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String admin = mvc.perform(get("/api/admin/support/users/{supportRef}/today", user.getSupportRef())
                        .header("Authorization", "Bearer " + login("support"))
                        .header("X-Audit-Reason", encoded(REASON)).param("date", DATE.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(admin)).isEqualTo(objectMapper.readTree(miniapp));
        assertThat(admin).doesNotContain(PRIVATE_OPENID).doesNotContain("openid");
    }

    @Test
    void supportRefsArePersistedOpaqueAndUnique() {
        String original = user.getSupportRef();
        User another = new User(); another.setOpenid("second-private-openid");
        another = users.saveAndFlush(another);

        assertThat(original).matches("usr_[a-f0-9]{32}");
        assertThat(another.getSupportRef()).matches("usr_[a-f0-9]{32}").isNotEqualTo(original);
        assertThat(users.findById(PRIVATE_OPENID).orElseThrow().getSupportRef()).isEqualTo(original);
        assertThat(original).doesNotContain(PRIVATE_OPENID);
    }

    @Test
    void mealDiagnosticsReadSavedSnapshotsAfterSystemFoodChanges() throws Exception {
        String superToken = login("super");
        mvc.perform(put("/api/admin/foods/{id}", food.getId())
                        .header("Authorization", "Bearer " + superToken)
                        .header("X-Audit-Reason", encoded("营养数据复核后调整"))
                        .contentType(MediaType.APPLICATION_JSON).content(foodJson("200")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.calories").value(200));

        mvc.perform(post("/api/foods/{id}/calculate", food.getId())
                        .header("Authorization", "Bearer " + jwtUtil.generateToken(PRIVATE_OPENID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":150}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.calories").value(300));

        String response = mvc.perform(get("/api/admin/support/users/{supportRef}/meals", user.getSupportRef())
                        .header("Authorization", "Bearer " + login("support"))
                        .header("X-Audit-Reason", encoded(REASON)).param("date", DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(150))
                .andExpect(jsonPath("$[0].unit").value("g"))
                .andExpect(jsonPath("$[0].snapshotCalories").value(165))
                .andExpect(jsonPath("$[0].snapshotProtein").value(31))
                .andExpect(jsonPath("$[0].idempotencyKey").value("meal-idempotency-001"))
                .andExpect(jsonPath("$[0].recordTime").exists())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain(PRIVATE_OPENID).doesNotContain("openid");
    }

    @Test
    void enforcesAuthenticationRoleAndAuditReason() throws Exception {
        String path = "/api/admin/support/users/" + user.getSupportRef();
        mvc.perform(get(path).header("X-Audit-Reason", encoded(REASON)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("ADMIN_AUTH_REQUIRED"));
        mvc.perform(get(path).header("Authorization", "Bearer " + login("food-editor"))
                        .header("X-Audit-Reason", encoded(REASON)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ADMIN_FORBIDDEN"));
        mvc.perform(get(path).header("Authorization", "Bearer " + login("support")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AUDIT_REASON_REQUIRED"));
        mvc.perform(get(path).header("Authorization", "Bearer " + login("support"))
                        .header("X-Audit-Reason", encoded(REASON)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.supportRef").value(user.getSupportRef()))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasKey("openid"))));
    }

    @Test
    void successfulQueriesAuditOperatorReasonObjectRequestResultAndTimeWithoutOpenid() throws Exception {
        String requestId = "support-request-20260715";
        String body = mvc.perform(get("/api/admin/support/users/{supportRef}/meals", user.getSupportRef())
                        .header("Authorization", "Bearer " + login("support"))
                        .header("X-Request-ID", requestId)
                        .header("X-Audit-Reason", encoded(REASON)).param("date", DATE.toString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<AdminAuditLog> logs = auditLogs.findAll();
        assertThat(logs).hasSize(1);
        AdminAuditLog log = logs.get(0);
        assertThat(log.getAdminUsername()).isEqualTo("support");
        assertThat(log.getAdminRole()).isEqualTo("SUPPORT_VIEWER");
        assertThat(log.getAction()).isEqualTo("READ_SUPPORT_MEALS");
        assertThat(log.getObjectType()).isEqualTo("SUPPORT_USER");
        assertThat(log.getObjectId()).isEqualTo(user.getSupportRef());
        assertThat(log.getRequestId()).isEqualTo(requestId);
        assertThat(log.getReason()).isEqualTo(REASON);
        assertThat(log.getResult()).isEqualTo("SUCCESS");
        assertThat(log.getCreatedAt()).isNotNull();
        assertThat(log.getAfterSummary()).contains("count=1").doesNotContain(PRIVATE_OPENID);
        assertThat(body).doesNotContain(PRIVATE_OPENID);
    }

    @Test
    void malformedReferencesCannotLeakSensitiveInputIntoAuditObjects() throws Exception {
        mvc.perform(get("/api/admin/support/users/{supportRef}", PRIVATE_OPENID)
                        .header("Authorization", "Bearer " + login("support"))
                        .header("X-Audit-Reason", encoded(REASON)))
                .andExpect(status().isNotFound());

        AdminAuditLog log = auditLogs.findAll().get(0);
        assertThat(log.getObjectId()).isEqualTo("INVALID_SUPPORT_REF");
        assertThat(String.join(" ", log.getObjectId(), String.valueOf(log.getBeforeSummary()),
                String.valueOf(log.getAfterSummary()))).doesNotContain(PRIVATE_OPENID);
    }

    private void createAdmin(String username, AdminRole role) {
        AdminUser admin = new AdminUser(); admin.setUsername(username); admin.setDisplayName(username);
        admin.setRole(role); admin.setPasswordHash(passwordEncoder.encode("correct-password-123"));
        admin.setEnabled(true); adminUsers.save(admin);
    }

    private String login(String username) throws Exception {
        String response = mvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct-password-123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String encoded(String value) { return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8); }

    private String foodJson(String calories) {
        return "{\"name\":\"快照鸡胸肉\",\"categoryId\":" + food.getCategory().getId()
                + ",\"baseAmount\":100,\"baseUnit\":\"g\",\"servingAmount\":null,\"servingUnit\":null"
                + ",\"calories\":" + calories + ",\"protein\":31,\"fat\":3.6,\"carbs\":0,\"source\":\"SYSTEM_EDITORIAL\"}";
    }
}
