package com.diettracker.repository;

import com.diettracker.entity.AccountDeletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface AccountDeletionAuditRepository extends JpaRepository<AccountDeletionAudit, String> {
    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}
