package com.diettracker.admin;

import com.diettracker.entity.AdminAuditLog;
import com.diettracker.repository.AdminAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {
    private final AdminAuditLogRepository logs;
    public AdminAuditService(AdminAuditLogRepository logs) { this.logs = logs; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AdminPrincipal principal, String action, String objectType, String objectId,
                       String requestId, String reason, String result, String before, String after) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(principal.id()); log.setAdminUsername(principal.username()); log.setAdminRole(principal.role().name());
        log.setAction(action); log.setObjectType(objectType); log.setObjectId(objectId); log.setRequestId(requestId);
        log.setReason(reason); log.setResult(result); log.setBeforeSummary(before); log.setAfterSummary(after);
        logs.save(log);
    }
}
