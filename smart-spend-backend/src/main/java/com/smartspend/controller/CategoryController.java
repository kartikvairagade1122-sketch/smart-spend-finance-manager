package com.smartspend.controller;

import com.smartspend.model.dto.category.CategoryRequest;
import com.smartspend.model.dto.category.CategoryResponse;
import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Endpoints for managing default and user-created income/expense categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all available categories for the authenticated user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "type", required = false) TransactionType type) {
        List<CategoryResponse> categories = (type != null)
                ? categoryService.getCategoriesForUserAndType(userDetails.getId(), type)
                : categoryService.getCategoriesForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    @Operation(summary = "Create a new custom category for the authenticated user")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing custom category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse updated = categoryService.updateCategory(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing custom category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        categoryService.deleteCategory(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
