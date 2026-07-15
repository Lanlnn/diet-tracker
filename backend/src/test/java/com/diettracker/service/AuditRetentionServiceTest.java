package com.diettracker.service;

import com.diettracker.repository.AccountDeletionAuditRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditRetentionServiceTest {
    @Test
    void purgesUsingTheConfiguredApplicationTimeZone() {
        AccountDeletionAuditRepository audits = mock(AccountDeletionAuditRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-15T16:30:00Z"), ZoneId.of("Asia/Shanghai"));

        new AuditRetentionService(audits, clock).purgeExpired();

        verify(audits).deleteByCreatedAtBefore(LocalDateTime.of(2026, 1, 17, 0, 30));
    }
}
