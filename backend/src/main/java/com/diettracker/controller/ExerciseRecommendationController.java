package com.diettracker.controller;

import com.diettracker.dto.ExerciseRecommendationResponse;
import com.diettracker.service.ExerciseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercise-recommendations")
public class ExerciseRecommendationController {
    private final ExerciseService service;

    public ExerciseRecommendationController(ExerciseService service) { this.service = service; }

    @GetMapping
    public List<ExerciseRecommendationResponse> get(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("userId") String userId) {
        return service.recommendations(date, userId);
    }
}
