package com.diettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_favorite", uniqueConstraints =
        @UniqueConstraint(name = "uk_food_favorite_user_food", columnNames = {"user_id", "food_item_id"}))
public class FoodFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String value) { this.userId = value; }
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem value) { this.foodItem = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
