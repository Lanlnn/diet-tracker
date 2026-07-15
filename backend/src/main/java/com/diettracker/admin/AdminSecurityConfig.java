package com.diettracker.admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSecurityConfig {
    @Bean
    PasswordEncoder adminPasswordEncoder() { return new BCryptPasswordEncoder(12); }
}
