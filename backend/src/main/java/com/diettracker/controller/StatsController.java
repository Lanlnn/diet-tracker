package com.diettracker.controller;

import com.diettracker.dto.TrendResponse;
import com.diettracker.service.TrendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final TrendService trendService;
    private final Clock clock;

    public StatsController(TrendService trendService, Clock clock) {
        this.trendService = trendService;
        this.clock = clock;
    }

    @GetMapping("/trend")
    public TrendResponse getTrend(
            @RequestAttribute("userId") String userId,
            @RequestParam(defaultValue = "7d") String range) {
        return trendService.getTrend(userId, range, LocalDate.now(clock));
    }
}
