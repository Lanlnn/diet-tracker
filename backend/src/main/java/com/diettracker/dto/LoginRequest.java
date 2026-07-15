package com.diettracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "登录 code 不能为空")
        @Size(max = 128, message = "登录 code 过长")
        String code) {
}
