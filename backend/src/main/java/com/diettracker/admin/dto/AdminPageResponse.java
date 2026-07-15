package com.diettracker.admin.dto;

import java.util.List;

public record AdminPageResponse<T>(List<T> items, int page, int size, long total, boolean hasNext) {
}
