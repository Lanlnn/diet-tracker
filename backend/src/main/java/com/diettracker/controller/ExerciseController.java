package com.diettracker.controller;

import com.diettracker.dto.ExerciseDayResponse;
import com.diettracker.dto.ExerciseRecordRequest;
import com.diettracker.dto.ExerciseResponse;
import com.diettracker.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService service;

    public ExerciseController(ExerciseService service) { this.service = service; }

    @GetMapping
    public ExerciseDayResponse getDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute("userId") String userId) {
        return service.getDay(date, userId);
    }

    @PostMapping
    public ExerciseResponse create(@Valid @RequestBody ExerciseRecordRequest request,
                                   @RequestAttribute("userId") String userId) {
        return service.create(request, userId);
    }

    @PutMapping("/{id}")
    public ExerciseResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ExerciseRecordRequest request,
                                   @RequestAttribute("userId") String userId) {
        return service.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestAttribute("userId") String userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
