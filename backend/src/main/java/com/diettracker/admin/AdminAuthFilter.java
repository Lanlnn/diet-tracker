package com.diettracker.admin;

import com.diettracker.api.ApiError;
import com.diettracker.api.RequestIdFilter;
import com.diettracker.entity.AdminUser;
import com.diettracker.repository.AdminUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(2)
public class AdminAuthFilter extends OncePerRequestFilter {
    private final AdminJwtUtil jwtUtil;
    private final AdminUserRepository users;
    private final ObjectMapper objectMapper;

    public AdminAuthFilter(AdminJwtUtil jwtUtil, AdminUserRepository users, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil; this.users = users; this.objectMapper = objectMapper;
    }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/admin/") || path.equals("/api/admin/auth/login")
                || "OPTIONS".equals(request.getMethod());
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            unauthorized(request, response, "ADMIN_AUTH_REQUIRED", "需要管理员登录后访问"); return;
        }
        AdminPrincipal principal;
        try {
            Claims claims = jwtUtil.parse(header.substring(7));
            Long id = Long.valueOf(claims.getSubject());
            Number sessionVersion = claims.get("sessionVersion", Number.class);
            AdminUser user = users.findById(id).filter(AdminUser::isEnabled).orElseThrow();
            if (sessionVersion == null || sessionVersion.longValue() != user.getSessionVersion()) throw new JwtException("Session invalidated");
            principal = new AdminPrincipal(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(), user.getSessionVersion());
        } catch (Exception exception) {
            unauthorized(request, response, "ADMIN_TOKEN_INVALID", "管理员登录状态已失效");
            return;
        }
        request.setAttribute(AdminRequestContext.PRINCIPAL, principal);
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletRequest request, HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(401); response.setCharacterEncoding("UTF-8"); response.setContentType("application/json");
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        objectMapper.writeValue(response.getWriter(), new ApiError(code, message,
                requestId == null ? "unknown" : requestId.toString(), Map.of()));
    }
}
