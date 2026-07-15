package com.diettracker.admin;

import com.diettracker.admin.dto.AdminSessionResponse;
import com.diettracker.api.ApiException;
import com.diettracker.entity.AdminUser;
import com.diettracker.repository.AdminUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {
    private final AdminUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtUtil jwtUtil;
    private final AdminLoginRateLimiter limiter;

    public AdminAuthService(AdminUserRepository users, PasswordEncoder passwordEncoder,
                            AdminJwtUtil jwtUtil, AdminLoginRateLimiter limiter) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.jwtUtil = jwtUtil; this.limiter = limiter;
    }

    @Transactional
    public AdminSessionResponse login(String username, String password, String remoteAddress) {
        String normalized = username == null ? "" : username.trim().toLowerCase();
        String key = normalized + "|" + remoteAddress;
        long retryAfter = limiter.retryAfterSeconds(key);
        if (retryAfter > 0) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ADMIN_LOGIN_RATE_LIMITED", "登录尝试过于频繁，请稍后再试");
        AdminUser user = users.findByUsernameIgnoreCase(normalized).orElse(null);
        if (user == null || !user.isEnabled() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            limiter.failure(key);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ADMIN_LOGIN_FAILED", "用户名或密码错误");
        }
        limiter.success(key);
        user.setLastLoginAt(LocalDateTime.now());
        users.save(user);
        AdminPrincipal principal = principal(user);
        return AdminSessionResponse.from(principal, jwtUtil.generate(principal), jwtUtil.getExpirationSeconds());
    }

    @Transactional
    public void logout(Long id) {
        AdminUser user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "ADMIN_AUTH_REQUIRED", "管理员不存在"));
        user.setSessionVersion(user.getSessionVersion() + 1);
        users.save(user);
    }

    public AdminPrincipal principal(AdminUser user) {
        return new AdminPrincipal(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(), user.getSessionVersion());
    }
}
