package com.diettracker.admin;

import com.diettracker.entity.AdminUser;
import com.diettracker.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;
    private final String displayName;

    public AdminBootstrap(AdminUserRepository users, PasswordEncoder encoder,
                          @Value("${admin.bootstrap.username:}") String username,
                          @Value("${admin.bootstrap.password:}") String password,
                          @Value("${admin.bootstrap.display-name:系统管理员}") String displayName) {
        this.users = users; this.encoder = encoder; this.username = username; this.password = password; this.displayName = displayName;
    }

    @Override public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) return;
        if (username.length() < 3 || password.length() < 12) {
            throw new IllegalStateException("Bootstrap admin requires username >= 3 and password >= 12 characters");
        }
        if (users.findByUsernameIgnoreCase(username.trim()).isPresent()) return;
        AdminUser user = new AdminUser();
        user.setUsername(username.trim().toLowerCase()); user.setDisplayName(displayName.trim());
        user.setPasswordHash(encoder.encode(password)); user.setRole(AdminRole.SUPER_ADMIN); user.setEnabled(true);
        users.save(user);
        log.info("Created bootstrap administrator username={}; remove bootstrap credentials after first start", user.getUsername());
    }
}
