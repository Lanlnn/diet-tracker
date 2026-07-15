package com.diettracker.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class AdminUserRefService {
    private final byte[] secret;
    public AdminUserRefService(@Value("${admin.user-ref.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }
    public String reference(String userId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return "usr_" + HexFormat.of().formatHex(mac.doFinal(userId.getBytes(StandardCharsets.UTF_8))).substring(0, 16);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create user reference", exception);
        }
    }
}
