package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.dto.UpdateProfileRequest;
import com.diettracker.dto.UserProfileResponse;
import com.diettracker.entity.User;
import com.diettracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        return UserProfileResponse.from(requireUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        user.setNickname(request.nickname().trim());
        user.setAvatarUrl(blankToNull(request.avatarUrl()));
        user.setGoalType(blankToNull(request.goalType()));
        user.setDailyCalorieGoal(request.dailyCalorieGoal());
        user.setCurrentWeight(request.currentWeight());
        user.setTargetWeight(request.targetWeight());
        return UserProfileResponse.from(userRepository.save(user));
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
