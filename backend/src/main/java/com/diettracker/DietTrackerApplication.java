package com.diettracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DietTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DietTrackerApplication.class, args);
    }
}
