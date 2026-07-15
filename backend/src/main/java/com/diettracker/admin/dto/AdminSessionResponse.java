package com.diettracker.admin.dto;

import com.diettracker.admin.AdminPrincipal;
import com.diettracker.admin.AdminRole;

public record AdminSessionResponse(Long id, String username, String displayName, AdminRole role,
                                   String token, long expiresIn) {
    public static AdminSessionResponse from(AdminPrincipal value, String token, long expiresIn) {
        return new AdminSessionResponse(value.id(), value.username(), value.displayName(), value.role(), token, expiresIn);
    }
}
