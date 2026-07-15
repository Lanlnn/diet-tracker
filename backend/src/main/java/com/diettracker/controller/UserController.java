package com.diettracker.controller;

import com.diettracker.dto.UpdateProfileRequest;
import com.diettracker.dto.UserProfileResponse;
import com.diettracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
