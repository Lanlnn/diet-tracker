package com.diettracker.security;

import com.diettracker.api.ApiError;
import com.diettracker.api.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Order(1)
public class AuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // Login, the local-only seed route, and CORS preflight may reach Spring MVC without a token.
        // Outside the local profile there is no seed controller, so the route must resolve to 404
        // instead of being masked as an authenticated endpoint by this filter.
        if (path.equals("/api/auth/login") || path.equals("/api/setup/seed") || "OPTIONS".equals(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Skip non-API paths
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for {}", path);
            res.setStatus(401);
            writeUnauthorized(req, res, "AUTH_REQUIRED", "需要登录后访问");
            return;
        }

        String token = authHeader.substring(7);
        JwtUtil.TokenStatus tokenStatus = jwtUtil.getTokenStatus(token);
        if (tokenStatus != JwtUtil.TokenStatus.VALID) {
            String errorCode = tokenStatus == JwtUtil.TokenStatus.EXPIRED ? "TOKEN_EXPIRED" : "TOKEN_INVALID";
            log.warn("Rejected {} token for {}", tokenStatus.name().toLowerCase(), path);
            res.setStatus(401);
            writeUnauthorized(req, res, errorCode, "登录状态已失效");
            return;
        }

        String openid = jwtUtil.extractOpenid(token);
        req.setAttribute("userId", openid);
        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        objectMapper.writeValue(response.getWriter(),
                new ApiError(code, message, value == null ? "unknown" : value.toString(), Map.of()));
    }
}
