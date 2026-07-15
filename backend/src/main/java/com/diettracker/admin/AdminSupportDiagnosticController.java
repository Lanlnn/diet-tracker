package com.diettracker.admin;

import com.diettracker.admin.dto.AdminMealDiagnosticResponse;
import com.diettracker.admin.dto.AdminSupportUserResponse;
import com.diettracker.dto.DashboardTodayResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/support/users")
@RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.SUPPORT_VIEWER})
public class AdminSupportDiagnosticController {
    private final AdminSupportDiagnosticService service;
    private final AdminUserRefService userRefs;

    public AdminSupportDiagnosticController(AdminSupportDiagnosticService service, AdminUserRefService userRefs) {
        this.service = service;
        this.userRefs = userRefs;
    }

    @GetMapping("/{supportRef}")
    @AdminAudit(action = "READ_SUPPORT_USER", objectType = "SUPPORT_USER")
    public AdminSupportUserResponse user(@PathVariable String supportRef, HttpServletRequest request) {
        auditObject(request, supportRef);
        AdminSupportUserResponse result = service.user(supportRef);
        request.setAttribute(AdminRequestContext.AUDIT_AFTER, "result=FOUND");
        return result;
    }

    @GetMapping("/{supportRef}/today")
    @AdminAudit(action = "READ_SUPPORT_TODAY", objectType = "SUPPORT_USER")
    public DashboardTodayResponse today(@PathVariable String supportRef,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        auditObject(request, supportRef);
        DashboardTodayResponse result = service.today(supportRef, date);
        request.setAttribute(AdminRequestContext.AUDIT_AFTER, "result=FOUND;date=" + date);
        return result;
    }

    @GetMapping("/{supportRef}/meals")
    @AdminAudit(action = "READ_SUPPORT_MEALS", objectType = "SUPPORT_USER")
    public List<AdminMealDiagnosticResponse> meals(@PathVariable String supportRef,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        auditObject(request, supportRef);
        List<AdminMealDiagnosticResponse> result = service.meals(supportRef, date);
        request.setAttribute(AdminRequestContext.AUDIT_AFTER, "result=FOUND;date=" + date + ";count=" + result.size());
        return result;
    }

    private void auditObject(HttpServletRequest request, String supportRef) {
        request.setAttribute(AdminRequestContext.AUDIT_OBJECT_ID,
                userRefs.isValid(supportRef) ? supportRef : "INVALID_SUPPORT_REF");
    }
}
