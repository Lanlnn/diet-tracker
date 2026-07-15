package com.diettracker.controller;

import com.diettracker.entity.User;
import com.diettracker.repository.UserRepository;
import com.diettracker.repository.UserGoalRepository;
import com.diettracker.repository.AccountDeletionAuditRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserProfileApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserGoalRepository userGoalRepository;
    @Autowired AccountDeletionAuditRepository auditRepository;

    private String tokenA;

    @BeforeEach
    void setUp() {
        userGoalRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(user("user-a", "用户甲"));
        userRepository.save(user("user-b", "用户乙"));
        tokenA = jwtUtil.generateToken("user-a");
    }

    @Test
    void managesGoalsAndDeletesAccountWithDoubleConfirmation() throws Exception {
        mockMvc.perform(put("/api/users/me/goals")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dailyCalorieGoal":1750,"carbsGoal":210,"proteinGoal":105,"fatGoal":55,
                                 "currentWeight":62.4,"targetWeight":58,"goalType":"LOSE_FAT","aiCoachEnabled":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proteinGoal").value(105))
                .andExpect(jsonPath("$.aiCoachEnabled").value(true));

        mockMvc.perform(delete("/api/users/me").header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELETE_CONFIRMATION_REQUIRED"));

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", bearer(tokenA))
                        .header("X-Delete-Confirmation", "DELETE"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(userRepository.existsById("user-a")).isFalse();
        org.assertj.core.api.Assertions.assertThat(userRepository.existsById("user-b")).isTrue();
        org.assertj.core.api.Assertions.assertThat(auditRepository.findAll()).anySatisfy(audit -> {
            org.assertj.core.api.Assertions.assertThat(audit.getUserHash()).hasSize(64).doesNotContain("user-a");
            org.assertj.core.api.Assertions.assertThat(audit.getRequestId()).isNotBlank();
        });
    }

    @Test
    void readsAndUpdatesOnlyAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("用户甲"))
                .andExpect(jsonPath("$", not(hasKey("openid"))));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"林晓","avatarUrl":"https://img.example/avatar.png",
                                 "goalType":"LOSE_FAT","dailyCalorieGoal":1800,
                                 "currentWeight":62.4,"targetWeight":58.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("林晓"))
                .andExpect(jsonPath("$.dailyCalorieGoal").value(1800));

        org.assertj.core.api.Assertions.assertThat(userRepository.findById("user-a").orElseThrow().getNickname())
                .isEqualTo("林晓");
        org.assertj.core.api.Assertions.assertThat(userRepository.findById("user-b").orElseThrow().getNickname())
                .isEqualTo("用户乙");
    }

    @Test
    void returnsStructuredValidationErrors() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"","avatarUrl":"javascript:alert(1)",
                                 "goalType":"RAPID_LOSS","dailyCalorieGoal":500}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.nickname").exists())
                .andExpect(jsonPath("$.fieldErrors.avatarUrl").exists())
                .andExpect(jsonPath("$.fieldErrors.goalType").exists())
                .andExpect(jsonPath("$.fieldErrors.dailyCalorieGoal").exists());
    }

    @Test
    void identifiesExpiredToken() throws Exception {
        JwtUtil expired = new JwtUtil("test-only-jwt-secret-with-more-than-thirty-two-bytes", -1);
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(expired.generateToken("user-a"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
    }

    private User user(String id, String nickname) {
        User user = new User();
        user.setOpenid(id);
        user.setNickname(nickname);
        return user;
    }

    private String bearer(String token) { return "Bearer " + token; }
}
