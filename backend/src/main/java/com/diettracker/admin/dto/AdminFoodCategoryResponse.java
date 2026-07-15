package com.diettracker.admin.dto;

import com.diettracker.entity.FoodCategory;
import java.time.LocalDateTime;

public record AdminFoodCategoryResponse(Long id, String name, String icon, Integer sortOrder,
                                        long systemFoodCount, LocalDateTime updatedAt) {
    public static AdminFoodCategoryResponse from(FoodCategory value, long count) {
        return new AdminFoodCategoryResponse(value.getId(), value.getName(), value.getIcon(),
                value.getSortOrder(), count, value.getUpdatedAt());
    }
}
