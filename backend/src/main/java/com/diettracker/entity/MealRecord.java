package com.diettracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_record")
public class MealRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Column(name = "meal_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(length = 500)
    private String note;

    @Column(name = "food_name_snapshot", nullable = false, length = 100)
    private String foodNameSnapshot;

    @Column(name = "base_amount_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmountSnapshot;

    @Column(name = "base_unit_snapshot", nullable = false, length = 20)
    private String baseUnitSnapshot;

    @Column(name = "calories_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal caloriesSnapshot;

    @Column(name = "protein_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal proteinSnapshot;

    @Column(name = "fat_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal fatSnapshot;

    @Column(name = "carbs_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbsSnapshot;

    @Column(name = "client_request_id", length = 100)
    private String clientRequestId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MealType {
        breakfast, lunch, dinner, snack
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (recordTime == null) recordTime = LocalDateTime.now();
        if (foodItem != null && foodNameSnapshot == null) {
            foodNameSnapshot = foodItem.getName();
            baseAmountSnapshot = foodItem.getBaseAmount();
            baseUnitSnapshot = foodItem.getBaseUnit();
            caloriesSnapshot = foodItem.getCalories();
            proteinSnapshot = foodItem.getProtein();
            fatSnapshot = foodItem.getFat();
            carbsSnapshot = foodItem.getCarbs();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getFoodNameSnapshot() { return foodNameSnapshot; }
    public void setFoodNameSnapshot(String value) { this.foodNameSnapshot = value; }
    public BigDecimal getBaseAmountSnapshot() { return baseAmountSnapshot; }
    public void setBaseAmountSnapshot(BigDecimal value) { this.baseAmountSnapshot = value; }
    public String getBaseUnitSnapshot() { return baseUnitSnapshot; }
    public void setBaseUnitSnapshot(String value) { this.baseUnitSnapshot = value; }
    public BigDecimal getCaloriesSnapshot() { return caloriesSnapshot; }
    public void setCaloriesSnapshot(BigDecimal value) { this.caloriesSnapshot = value; }
    public BigDecimal getProteinSnapshot() { return proteinSnapshot; }
    public void setProteinSnapshot(BigDecimal value) { this.proteinSnapshot = value; }
    public BigDecimal getFatSnapshot() { return fatSnapshot; }
    public void setFatSnapshot(BigDecimal value) { this.fatSnapshot = value; }
    public BigDecimal getCarbsSnapshot() { return carbsSnapshot; }
    public void setCarbsSnapshot(BigDecimal value) { this.carbsSnapshot = value; }
    public String getClientRequestId() { return clientRequestId; }
    public void setClientRequestId(String value) { this.clientRequestId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
