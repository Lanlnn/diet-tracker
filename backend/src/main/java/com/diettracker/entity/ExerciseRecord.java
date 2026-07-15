package com.diettracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exercise_record")
public class ExerciseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_date", nullable = false)
    private LocalDate exerciseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 30)
    private ExerciseType exerciseType;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Intensity intensity;

    @Column(name = "calories_burned", nullable = false, precision = 10, scale = 2)
    private BigDecimal caloriesBurned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Source source;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ExerciseType { walking, running, cycling, strength, yoga, stretching, other }
    public enum Intensity { low, medium, high }
    public enum Source { MANUAL, RECOMMENDATION }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getExerciseDate() { return exerciseDate; }
    public void setExerciseDate(LocalDate exerciseDate) { this.exerciseDate = exerciseDate; }
    public ExerciseType getExerciseType() { return exerciseType; }
    public void setExerciseType(ExerciseType exerciseType) { this.exerciseType = exerciseType; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Intensity getIntensity() { return intensity; }
    public void setIntensity(Intensity intensity) { this.intensity = intensity; }
    public BigDecimal getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(BigDecimal caloriesBurned) { this.caloriesBurned = caloriesBurned; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
