package com.diettracker.admin.dto;

import com.diettracker.admin.AdminPrincipal;
import com.diettracker.admin.AdminRole;

public record AdminProfileResponse(Long id, String username, String displayName, AdminRole role) {
    public static AdminProfileResponse from(AdminPrincipal value) {
        return new AdminProfileResponse(value.id(), value.username(), value.displayName(), value.role());
    }
}
