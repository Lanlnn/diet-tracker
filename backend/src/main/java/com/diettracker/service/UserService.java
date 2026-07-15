package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.ProfileSummaryResponse;
import com.diettracker.dto.UpdateProfileRequest;
import com.diettracker.dto.UserProfileResponse;
import com.diettracker.entity.AccountDeletionAudit;
import com.diettracker.entity.User;
import com.diettracker.entity.UserGoal;
import com.diettracker.repository.AccountDeletionAuditRepository;
import com.diettracker.repository.ExerciseRecordRepository;
import com.diettracker.repository.FoodFavoriteRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserGoalRepository;
import com.diettracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository users;
    private final UserGoalRepository goals;
    private final UserGoalService goalService;
    private final MealRecordRepository meals;
    private final ExerciseRecordRepository exercises;
    private final FoodFavoriteRepository favorites;
    private final FoodItemRepository foods;
    private final AccountDeletionAuditRepository audits;
    private final AvatarStorageService avatarStorage;
    private final String auditPepper;
    private final Clock clock;

    public UserService(UserRepository users, UserGoalRepository goals, UserGoalService goalService,
                       MealRecordRepository meals, ExerciseRecordRepository exercises,
                       FoodFavoriteRepository favorites, FoodItemRepository foods,
                       AccountDeletionAuditRepository audits, AvatarStorageService avatarStorage,
                       @Value("${app.deletion.audit-pepper}") String auditPepper, Clock clock) {
        this.users = users;
        this.goals = goals;
        this.goalService = goalService;
        this.meals = meals;
        this.exercises = exercises;
        this.favorites = favorites;
        this.foods = foods;
        this.audits = audits;
        this.avatarStorage = avatarStorage;
        this.auditPepper = auditPepper;
        this.clock = clock;
    }

    @Transactional
    public UserProfileResponse getProfile(String userId) {
        User user = requireUser(userId);
        return UserProfileResponse.from(user, goalService.requireOrCreate(userId), streakDays(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        String oldAvatar = user.getAvatarUrl();
        user.setNickname(request.nickname().trim());
        user.setAvatarUrl(blankToNull(request.avatarUrl()));
        UserGoal goal = goalService.requireOrCreate(userId);
        if (request.dailyCalorieGoal() != null) {
            int calories = request.dailyCalorieGoal();
            goal.setDailyCalorieGoal(calories);
            goal.setCarbsGoal(UserGoalService.macroGoal(calories, "carbs"));
            goal.setProteinGoal(UserGoalService.macroGoal(calories, "protein"));
            goal.setFatGoal(UserGoalService.macroGoal(calories, "fat"));
        }
        if (request.goalType() != null && !request.goalType().isBlank()) goal.setGoalType(request.goalType());
        goal.setCurrentWeight(request.currentWeight());
        goal.setTargetWeight(request.targetWeight());
        goal.setCustomized(true);
        users.save(user);
        goals.save(goal);
        if (oldAvatar != null && !oldAvatar.equals(user.getAvatarUrl())) stageAvatarDeletion(oldAvatar);
        return UserProfileResponse.from(user, goal, streakDays(userId));
    }

    @Transactional(readOnly = true)
    public ProfileSummaryResponse summary(String userId) {
        requireUser(userId);
        LocalDate today = LocalDate.now(clock);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new ProfileSummaryResponse(foods.countByUserId(userId), favorites.countByUserId(userId),
                exercises.countByUserIdAndExerciseDateBetween(userId, monday, today), streakDays(userId));
    }

    @Transactional
    public void deleteAccount(String userId, String confirmation, String requestId) {
        if (!"DELETE".equals(confirmation)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DELETE_CONFIRMATION_REQUIRED", "请输入 DELETE 二次确认");
        }
        User user = requireUser(userId);
        stageAvatarDeletion(user.getAvatarUrl());
        meals.deleteByUserId(userId);
        exercises.deleteByUserId(userId);
        favorites.deleteByUserId(userId);
        foods.deleteByUserId(userId);
        goals.deleteById(userId);
        users.delete(user);

        AccountDeletionAudit audit = new AccountDeletionAudit();
        audit.setEventId(UUID.randomUUID().toString());
        audit.setUserHash(hash(userId));
        audit.setRequestId(requestId);
        audit.setResult("SUCCESS");
        audit.setCreatedAt(LocalDateTime.now(clock));
        audits.save(audit);
    }

    private int streakDays(String userId) {
        List<LocalDate> dates = meals.findRecordedDatesDesc(userId);
        if (dates.isEmpty()) return 0;
        LocalDate expected = LocalDate.now(clock);
        if (dates.get(0).isBefore(expected)) expected = expected.minusDays(1);
        int streak = 0;
        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (date.isBefore(expected)) break;
        }
        return streak;
    }

    private User requireUser(String userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
    }

    private String hash(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest((auditPepper + value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void stageAvatarDeletion(String avatarUrl) {
        AvatarStorageService.StagedDelete staged = avatarStorage.stageDeleteManagedAvatar(avatarUrl);
        if (staged == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) staged.commit();
                else staged.rollback();
            }
        });
    }
}
