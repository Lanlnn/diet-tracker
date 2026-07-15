package com.diettracker.admin;

import com.diettracker.admin.dto.*;
import com.diettracker.dto.NutritionCalculationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminFoodController {
    private static final AdminRole[] FOOD_ROLES = {AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR};
    private final AdminFoodService service;
    private final ObjectMapper objectMapper;
    public AdminFoodController(AdminFoodService service, ObjectMapper objectMapper) {
        this.service = service; this.objectMapper = objectMapper;
    }

    @GetMapping("/foods") @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    public AdminPageResponse<AdminFoodResponse> foods(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId, @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort, @RequestParam(defaultValue = "desc") String direction) {
        return service.list(keyword, categoryId, source, page, size, sort, direction);
    }

    @PostMapping("/foods") @ResponseStatus(HttpStatus.CREATED)
    @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    @AdminAudit(action = "CREATE_FOOD", objectType = "SYSTEM_FOOD")
    public AdminFoodResponse createFood(@Valid @RequestBody AdminFoodInput body, HttpServletRequest request) {
        AdminFoodResponse result = service.create(body); audit(request, result.id(), null, result); return result;
    }

    @PutMapping("/foods/{id}") @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    @AdminAudit(action = "UPDATE_FOOD", objectType = "SYSTEM_FOOD")
    public AdminFoodResponse updateFood(@PathVariable Long id, @Valid @RequestBody AdminFoodInput body, HttpServletRequest request) {
        AdminFoodResponse before = service.getSystemFood(id); AdminFoodResponse result = service.update(id, body);
        audit(request, id, before, result); return result;
    }

    @PostMapping("/foods/preview") @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    public NutritionCalculationResponse preview(@Valid @RequestBody AdminFoodPreviewRequest body) { return service.preview(body); }

    @GetMapping("/food-categories") @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    public List<AdminFoodCategoryResponse> categories() { return service.listCategories(); }

    @PostMapping("/food-categories") @ResponseStatus(HttpStatus.CREATED)
    @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    @AdminAudit(action = "CREATE_FOOD_CATEGORY", objectType = "FOOD_CATEGORY")
    public AdminFoodCategoryResponse createCategory(@Valid @RequestBody AdminFoodCategoryInput body, HttpServletRequest request) {
        AdminFoodCategoryResponse result = service.createCategory(body); audit(request, result.id(), null, result); return result;
    }

    @PutMapping("/food-categories/{id}") @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    @AdminAudit(action = "UPDATE_FOOD_CATEGORY", objectType = "FOOD_CATEGORY")
    public AdminFoodCategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody AdminFoodCategoryInput body,
                                                     HttpServletRequest request) {
        AdminFoodCategoryResponse before = service.listCategories().stream().filter(value -> value.id().equals(id)).findFirst().orElse(null);
        AdminFoodCategoryResponse result = service.updateCategory(id, body); audit(request, id, before, result); return result;
    }

    @DeleteMapping("/food-categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR})
    @AdminAudit(action = "DELETE_FOOD_CATEGORY", objectType = "FOOD_CATEGORY")
    public void deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        AdminFoodCategoryResponse before = service.listCategories().stream().filter(value -> value.id().equals(id)).findFirst().orElse(null);
        request.setAttribute(AdminRequestContext.AUDIT_OBJECT_ID, id.toString());
        request.setAttribute(AdminRequestContext.AUDIT_BEFORE, json(before)); service.deleteCategory(id);
    }

    @GetMapping("/custom-foods")
    @RequireAdminRoles({AdminRole.SUPER_ADMIN, AdminRole.FOOD_EDITOR, AdminRole.SUPPORT_VIEWER})
    public List<CustomFoodDiagnosticResponse> customFoods(@RequestParam String userRef, @RequestParam Long foodId) {
        return List.of(service.diagnoseCustomFood(userRef, foodId));
    }

    private void audit(HttpServletRequest request, Long id, Object before, Object after) {
        request.setAttribute(AdminRequestContext.AUDIT_OBJECT_ID, id.toString());
        request.setAttribute(AdminRequestContext.AUDIT_BEFORE, json(before));
        request.setAttribute(AdminRequestContext.AUDIT_AFTER, json(after));
    }
    private String json(Object value) {
        if (value == null) return null;
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { return "{\"summary\":\"unavailable\"}"; }
    }
}
