package com.diettracker.controller;

import com.diettracker.dto.CalendarSummaryResponse;
import com.diettracker.service.CalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService calendarService;
    private final Clock clock;

    public CalendarController(CalendarService calendarService, Clock clock) {
        this.calendarService = calendarService;
        this.clock = clock;
    }

    @GetMapping("/summary")
    public CalendarSummaryResponse getSummary(
            @RequestAttribute("userId") String userId,
            @RequestParam String month) {
        return calendarService.getSummary(userId, month, LocalDate.now(clock));
    }
}
