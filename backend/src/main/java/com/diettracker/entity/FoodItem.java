package com.diettracker.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "food_item")
public class FoodItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private FoodCategory category;
    @Column(length = 20)
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount = new BigDecimal("100");
    @Column(name = "base_unit", nullable = false, length = 20)
    private String baseUnit = "g";
    @Column(name = "serving_amount", precision = 10, scale = 2)
    private BigDecimal servingAmount;
    @Column(name = "serving_unit", length = 20)
    private String servingUnit;
    @Column(nullable = false, length = 30)
    private String source = "SYSTEM";
    @Column(name = "user_id", length = 100)
    private String userId;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory c) { this.category = c; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getCalories() { return calories; }
    public void setCalories(BigDecimal c) { this.calories = c; }
    public BigDecimal getProtein() { return protein; }
    public void setProtein(BigDecimal p) { this.protein = p; }
    public BigDecimal getFat() { return fat; }
    public void setFat(BigDecimal f) { this.fat = f; }
    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal c) { this.carbs = c; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal value) { this.baseAmount = value; }
    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String value) { this.baseUnit = value; }
    public BigDecimal getServingAmount() { return servingAmount; }
    public void setServingAmount(BigDecimal value) { this.servingAmount = value; }
    public String getServingUnit() { return servingUnit; }
    public void setServingUnit(String value) { this.servingUnit = value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source = value; }
    public String getUserId() { return userId; }
    public void setUserId(String u) { this.userId = u; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}
