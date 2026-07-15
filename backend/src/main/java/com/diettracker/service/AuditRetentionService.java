package com.diettracker.service;

import com.diettracker.repository.AccountDeletionAuditRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AuditRetentionService {
    private final AccountDeletionAuditRepository audits;
    private final Clock clock;

    public AuditRetentionService(AccountDeletionAuditRepository audits, Clock clock) {
        this.audits = audits;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.deletion.audit-cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void purgeExpired() {
        audits.deleteByCreatedAtBefore(LocalDateTime.now(clock).minusDays(180));
    }
}
