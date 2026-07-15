package com.diettracker.admin;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class AdminJwtUtil {
    public static final String AUDIENCE = "diet-tracker-admin";
    private final SecretKey key;
    private final long expiration;

    public AdminJwtUtil(@Value("${admin.jwt.secret}") String secret,
                        @Value("${admin.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generate(AdminPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.id().toString())
                .audience().add(AUDIENCE).and()
                .claim("username", principal.username())
                .claim("role", principal.role().name())
                .claim("sessionVersion", principal.sessionVersion())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (claims.getAudience() == null || !claims.getAudience().contains(AUDIENCE)) {
            throw new JwtException("Invalid admin token audience");
        }
        return claims;
    }

    public long getExpirationSeconds() { return expiration / 1000; }
}
