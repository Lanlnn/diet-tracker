package com.diettracker.admin;

public record AdminPrincipal(Long id, String username, String displayName, AdminRole role, long sessionVersion) {
}
