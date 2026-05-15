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
    public ResponseEntity<MealRecord> addRecord(@RequestBody MealRecord record) {
        MealRecord saved = service.addRecord(record);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<MealRecord> getRecords(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getRecordsByDate(date);
    }

    @GetMapping("/range")
    public List<MealRecord> getRecordsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.getRecordsByDateRange(start, end);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/daily")
    public Map<String, Object> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getDailyStats(date);
    }

    @GetMapping("/stats/weekly")
    public Map<String, Object> getWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getWeeklyStats(date);
    }
}
