package com.diettracker.controller;

import com.diettracker.service.SetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private static final Logger log = LoggerFactory.getLogger(SetupController.class);

    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        log.info(">>> Manual seed triggered via API");
        Map<String, Object> result = setupService.seed();
        return ResponseEntity.ok(result);
    }
}
