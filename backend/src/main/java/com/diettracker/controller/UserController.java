package com.diettracker.controller;

import com.diettracker.dto.UpdateProfileRequest;
import com.diettracker.dto.UpdateGoalRequest;
import com.diettracker.dto.UserGoalResponse;
import com.diettracker.dto.ProfileSummaryResponse;
import com.diettracker.api.RequestIdFilter;
import com.diettracker.service.UserGoalService;
import com.diettracker.dto.UserProfileResponse;
import com.diettracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserGoalService goalService;

    public UserController(UserService userService, UserGoalService goalService) {
        this.userService = userService;
        this.goalService = goalService;
    }

    @GetMapping("/me")
    public UserProfileResponse getProfile(@RequestAttribute("userId") String userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/me/goals")
    public UserGoalResponse getGoals(@RequestAttribute("userId") String userId) {
        return goalService.get(userId);
    }

    @PutMapping("/me/goals")
    public UserGoalResponse updateGoals(@RequestAttribute("userId") String userId,
                                        @Valid @RequestBody UpdateGoalRequest request) {
        return goalService.update(userId, request);
    }

    @GetMapping("/me/summary")
    public ProfileSummaryResponse summary(@RequestAttribute("userId") String userId) {
        return userService.summary(userId);
    }

    @DeleteMapping("/me")
    public void deleteAccount(@RequestAttribute("userId") String userId,
                              @RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId,
                              @RequestHeader(value = "X-Delete-Confirmation", required = false) String confirmation) {
        userService.deleteAccount(userId, confirmation, requestId);
    }
}
