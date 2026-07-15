package com.diettracker.admin;

import com.diettracker.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public final class AdminRequestContext {
    public static final String PRINCIPAL = "adminPrincipal";
    public static final String AUDIT_OBJECT_ID = "adminAuditObjectId";
    public static final String AUDIT_BEFORE = "adminAuditBefore";
    public static final String AUDIT_AFTER = "adminAuditAfter";

    private AdminRequestContext() {}

    public static AdminPrincipal require(HttpServletRequest request) {
        Object principal = request.getAttribute(PRINCIPAL);
        if (principal instanceof AdminPrincipal value) return value;
        throw new ApiException(HttpStatus.UNAUTHORIZED, "ADMIN_AUTH_REQUIRED", "需要管理员登录后访问");
    }
}
