package com.diettracker.admin;

import com.diettracker.admin.dto.AdminLoginRequest;
import com.diettracker.admin.dto.AdminProfileResponse;
import com.diettracker.admin.dto.AdminSessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService authService;
    public AdminAuthController(AdminAuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public AdminSessionResponse login(@Valid @RequestBody AdminLoginRequest body, HttpServletRequest request) {
        return authService.login(body.username(), body.password(), request.getRemoteAddr());
    }

    @GetMapping("/me")
    public AdminProfileResponse me(HttpServletRequest request) {
        return AdminProfileResponse.from(AdminRequestContext.require(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(AdminRequestContext.require(request).id());
        return ResponseEntity.noContent().build();
    }
}
