package com.diettracker.dto;

public record LoginResponse(
        String token,
        long expiresIn,
        UserProfileResponse user) {
}
