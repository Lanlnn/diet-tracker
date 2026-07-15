package com.diettracker.security;

import com.diettracker.api.ApiError;
import com.diettracker.api.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(2)
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true")
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int requestsPerMinute;

    public RateLimitFilter(ObjectMapper objectMapper,
                           @Value("${app.rate-limit.requests-per-minute:120}") int requestsPerMinute) {
        this.objectMapper = objectMapper;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String userId = (String) request.getAttribute("userId");
        String key = userId == null ? request.getRemoteAddr() : userId;
        long minute = Instant.now().getEpochSecond() / 60;
        Window window = windows.compute(key, (ignored, current) ->
                current == null || current.minute != minute ? new Window(minute) : current);
        if (window.count.incrementAndGet() > requestsPerMinute) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", "60");
            Object id = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
            objectMapper.writeValue(response.getWriter(), new ApiError(
                    "RATE_LIMITED", "请求过于频繁，请稍后重试", id == null ? "unknown" : id.toString(), Map.of()));
            return;
        }
        chain.doFilter(request, response);
        if (windows.size() > 10_000) windows.entrySet().removeIf(entry -> entry.getValue().minute < minute - 1);
    }

    private static final class Window {
        private final long minute;
        private final AtomicInteger count = new AtomicInteger();
        private Window(long minute) { this.minute = minute; }
    }
}
