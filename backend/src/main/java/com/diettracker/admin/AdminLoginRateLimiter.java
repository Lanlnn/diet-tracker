package com.diettracker.admin;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminLoginRateLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public long retryAfterSeconds(String key) {
        Attempt value = attempts.get(key);
        if (value == null || value.lockedUntil == null) return 0;
        long seconds = Duration.between(Instant.now(), value.lockedUntil).toSeconds();
        if (seconds <= 0) { attempts.remove(key); return 0; }
        return seconds;
    }

    public void failure(String key) {
        attempts.compute(key, (ignored, old) -> {
            Instant now = Instant.now();
            Attempt value = old == null || Duration.between(old.windowStart, now).compareTo(WINDOW) > 0
                    ? new Attempt(now, 0, null) : old;
            int failures = value.failures + 1;
            return new Attempt(value.windowStart, failures,
                    failures >= MAX_FAILURES ? now.plus(LOCK) : value.lockedUntil);
        });
    }

    public void success(String key) { attempts.remove(key); }
    private record Attempt(Instant windowStart, int failures, Instant lockedUntil) {}
}
