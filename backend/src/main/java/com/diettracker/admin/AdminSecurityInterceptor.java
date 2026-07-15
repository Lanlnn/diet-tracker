package com.diettracker.admin;

import com.diettracker.api.ApiException;
import com.diettracker.api.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {
    private final AdminAuditService auditService;
    public AdminSecurityInterceptor(AdminAuditService auditService) { this.auditService = auditService; }

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method) || !request.getRequestURI().startsWith("/api/admin/")
                || request.getRequestURI().equals("/api/admin/auth/login")) return true;
        AdminPrincipal principal = AdminRequestContext.require(request);
        RequireAdminRoles roles = AnnotationUtils.findAnnotation(method.getMethod(), RequireAdminRoles.class);
        if (roles == null) roles = AnnotationUtils.findAnnotation(method.getBeanType(), RequireAdminRoles.class);
        if (roles != null && Arrays.stream(roles.value()).noneMatch(role -> role == principal.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_FORBIDDEN", "当前管理员无权执行此操作");
        }
        AdminAudit audit = AnnotationUtils.findAnnotation(method.getMethod(), AdminAudit.class);
        if (audit != null) {
            String reason = auditReason(request);
            if (reason == null || reason.trim().length() < 2 || reason.length() > 200) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "AUDIT_REASON_REQUIRED", "写操作必须填写 2-200 字审计原因");
            }
        }
        return true;
    }

    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod method)) return;
        AdminAudit audit = AnnotationUtils.findAnnotation(method.getMethod(), AdminAudit.class);
        Object value = request.getAttribute(AdminRequestContext.PRINCIPAL);
        if (audit == null || !(value instanceof AdminPrincipal principal)) return;
        auditService.record(principal, audit.action(), audit.objectType(), stringAttr(request, AdminRequestContext.AUDIT_OBJECT_ID),
                stringAttr(request, RequestIdFilter.REQUEST_ID_ATTRIBUTE), auditReason(request),
                ex == null && response.getStatus() < 400 ? "SUCCESS" : "FAILURE",
                stringAttr(request, AdminRequestContext.AUDIT_BEFORE), stringAttr(request, AdminRequestContext.AUDIT_AFTER));
    }

    private String stringAttr(HttpServletRequest request, String key) {
        Object value = request.getAttribute(key); return value == null ? null : value.toString();
    }

    private String auditReason(HttpServletRequest request) {
        String value = request.getHeader("X-Audit-Reason");
        if (value == null) return null;
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUDIT_REASON_INVALID", "审计原因编码无效");
        }
    }
}
