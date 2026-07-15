package com.diettracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProfileRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 40, message = "昵称最多 40 个字符")
        String nickname,

        @Size(max = 500, message = "头像地址过长")
        @Pattern(regexp = "^$|^https?://[^\\s]+$", message = "头像地址格式不正确")
        String avatarUrl,

        @Size(max = 20, message = "目标类型过长")
        @Pattern(regexp = "^$|LOSE_FAT|MAINTAIN|BUILD_MUSCLE", message = "目标类型不正确")
        String goalType,

        @Min(value = 1000, message = "每日热量目标不能低于 1000")
        @Max(value = 5000, message = "每日热量目标不能高于 5000")
        Integer dailyCalorieGoal,

        @DecimalMin(value = "20.0", message = "当前体重不能低于 20kg")
        @DecimalMax(value = "500.0", message = "当前体重不能高于 500kg")
        BigDecimal currentWeight,

        @DecimalMin(value = "20.0", message = "目标体重不能低于 20kg")
        @DecimalMax(value = "500.0", message = "目标体重不能高于 500kg")
        BigDecimal targetWeight) {
}
