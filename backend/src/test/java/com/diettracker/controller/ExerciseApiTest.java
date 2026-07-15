package com.diettracker.controller;

import com.diettracker.entity.ExerciseRecord;
import com.diettracker.entity.User;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.repository.UserRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExerciseApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired ExerciseRecordRepository exerciseRepository;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;

    private final LocalDate date = LocalDate.of(2026, 7, 15);
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(user("user-a"));
        userRepository.save(user("user-b"));
        tokenA = jwtUtil.generateToken("user-a");
        tokenB = jwtUtil.generateToken("user-b");
    }

    @AfterEach
    void tearDown() { exerciseRepository.deleteAll(); }

    @Test
    void completesCreateListUpdateDeleteAndDashboardLoop() throws Exception {
        String body = """
                {"exerciseDate":"2026-07-15","exerciseType":"walking","startTime":"18:00",
                 "durationMinutes":20,"intensity":"medium","source":"MANUAL","note":"饭后"}
                """;

        String response = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", bearer(tokenA)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caloriesBurned").value(80))
                .andExpect(jsonPath("$.typeLabel").value("户外快走"))
                .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/exercises").header("Authorization", bearer(tokenA)).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(80))
                .andExpect(jsonPath("$.totalDurationMinutes").value(20))
                .andExpect(jsonPath("$.records.length()").value(1))
                .andExpect(jsonPath("$.weeklyCompletion.completedDays").value(1));

        mockMvc.perform(get("/api/dashboard/today").header("Authorization", bearer(tokenA)).param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingCalories").value(1800))
                .andExpect(jsonPath("$.exerciseCalories").value(80))
                .andExpect(jsonPath("$.netCalories").value(-80))
                .andExpect(jsonPath("$.exercise.state").value("success"));

        String updateBody = body.replace("20", "25").replace("\"note\":\"饭后\"", "\"caloriesBurned\":90,\"note\":\"饭后\"");
        mockMvc.perform(put("/api/exercises/{id}", id).header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.caloriesBurned").value(90));

        mockMvc.perform(delete("/api/exercises/{id}", id).header("Authorization", bearer(tokenA)))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsCrossUserMutationAndLimitsSafeRecommendations() throws Exception {
        ExerciseRecord record = exerciseRepository.save(record("user-a"));
        String body = """
                {"exerciseDate":"2026-07-15","exerciseType":"running","startTime":"18:00",
                 "durationMinutes":20,"intensity":"medium","caloriesBurned":160,"source":"MANUAL"}
                """;
        mockMvc.perform(put("/api/exercises/{id}", record.getId()).header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/exercises/{id}", record.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/exercise-recommendations").header("Authorization", bearer(tokenB))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].intensity").value("low"))
                .andExpect(jsonPath("$[0].reason").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("治疗"))));
    }

    private User user(String id) {
        User user = new User();
        user.setOpenid(id);
        user.setNickname(id);
        user.setDailyCalorieGoal(1800);
        return user;
    }

    private ExerciseRecord record(String userId) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setExerciseDate(date);
        record.setExerciseType(ExerciseRecord.ExerciseType.walking);
        record.setStartTime(LocalTime.of(18, 0));
        record.setDurationMinutes(20);
        record.setIntensity(ExerciseRecord.Intensity.medium);
        record.setCaloriesBurned(new BigDecimal("80"));
        record.setSource(ExerciseRecord.Source.MANUAL);
        return record;
    }

    private String bearer(String token) { return "Bearer " + token; }
}
