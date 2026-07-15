package com.diettracker.admin.dto;

import jakarta.validation.constraints.*;

public record AdminFoodCategoryInput(@NotBlank @Size(max = 50) String name,
                                     @Size(max = 100) String icon,
                                     @NotNull @Min(0) @Max(9999) Integer sortOrder) {
}
