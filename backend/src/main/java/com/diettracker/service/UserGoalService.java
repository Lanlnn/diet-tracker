package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.UpdateGoalRequest;
import com.diettracker.dto.UserGoalResponse;
import com.diettracker.entity.UserGoal;
import com.diettracker.repository.UserGoalRepository;
import com.diettracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UserGoalService {
    private final UserGoalRepository goals;
    private final UserRepository users;

    public UserGoalService(UserGoalRepository goals, UserRepository users) {
        this.goals = goals;
        this.users = users;
    }

    @Transactional
    public UserGoal requireOrCreate(String userId) {
        return goals.findById(userId).orElseGet(() -> {
            if (!users.existsById(userId)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在");
            }
            UserGoal value = new UserGoal();
            value.setUserId(userId);
            return goals.save(value);
        });
    }

    @Transactional(readOnly = true)
    public UserGoal findOrDefault(String userId) {
        return goals.findById(userId).orElseGet(() -> defaultGoal(userId));
    }

    @Transactional
    public UserGoalResponse get(String userId) {
        return UserGoalResponse.from(requireOrCreate(userId));
    }

    @Transactional
    public UserGoalResponse update(String userId, UpdateGoalRequest request) {
        UserGoal value = requireOrCreate(userId);
        apply(value, request);
        return UserGoalResponse.from(goals.save(value));
    }

    public void apply(UserGoal value, UpdateGoalRequest request) {
        value.setDailyCalorieGoal(request.dailyCalorieGoal());
        value.setCarbsGoal(request.carbsGoal());
        value.setProteinGoal(request.proteinGoal());
        value.setFatGoal(request.fatGoal());
        value.setCurrentWeight(request.currentWeight());
        value.setTargetWeight(request.targetWeight());
        value.setGoalType(request.goalType());
        value.setAiCoachEnabled(request.aiCoachEnabled());
        value.setCustomized(true);
    }

    public UserGoal defaultGoal(String userId) {
        UserGoal value = new UserGoal();
        value.setUserId(userId);
        return value;
    }

    public static BigDecimal macroGoal(int calories, String macro) {
        BigDecimal ratio = switch (macro) {
            case "carbs" -> new BigDecimal("0.50");
            case "protein" -> new BigDecimal("0.20");
            default -> new BigDecimal("0.30");
        };
        int divisor = "fat".equals(macro) ? 9 : 4;
        return BigDecimal.valueOf(calories).multiply(ratio)
                .divide(BigDecimal.valueOf(divisor), 1, RoundingMode.HALF_UP);
    }
}
