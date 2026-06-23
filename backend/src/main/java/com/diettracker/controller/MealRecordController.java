package com.diettracker.controller;

import com.diettracker.model.MealRecord;
import com.diettracker.service.MealRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class MealRecordController {

    private final MealRecordService service;

    public MealRecordController(MealRecordService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MealRecord> addRecord(
            @RequestBody MealRecord record,
            @RequestAttribute("userId") String userId) {
        MealRecord saved = service.addRecord(record, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<MealRecord> getRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("userId") String userId) {
        return service.getRecordsByDate(date, userId);
    }

    @GetMapping("/range")
    public List<MealRecord> getRecordsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestAttribute("userId") String userId) {
        return service.getRecordsByDateRange(start, end, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable Long id,
            @RequestAttribute("userId") String userId) {
        service.deleteRecord(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/daily")
    public Map<String, Object> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("userId") String userId) {
        return service.getDailyStats(date, userId);
    }

    @GetMapping("/stats/weekly")
    public Map<String, Object> getWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("userId") String userId) {
        return service.getWeeklyStats(date, userId);
    }
}
