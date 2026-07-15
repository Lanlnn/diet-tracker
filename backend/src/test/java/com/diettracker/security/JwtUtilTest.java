package com.diettracker.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    private static final String SECRET = "test-only-jwt-secret-with-more-than-thirty-two-bytes";

    @Test
    void distinguishesValidExpiredAndInvalidTokens() {
        JwtUtil valid = new JwtUtil(SECRET, 60_000);
        JwtUtil expired = new JwtUtil(SECRET, -1);

        assertThat(valid.getTokenStatus(valid.generateToken("user-a")))
                .isEqualTo(JwtUtil.TokenStatus.VALID);
        assertThat(expired.getTokenStatus(expired.generateToken("user-a")))
                .isEqualTo(JwtUtil.TokenStatus.EXPIRED);
        assertThat(valid.getTokenStatus("not-a-jwt"))
                .isEqualTo(JwtUtil.TokenStatus.INVALID);
        assertThat(valid.getExpirationSeconds()).isEqualTo(60L);
    }
}
