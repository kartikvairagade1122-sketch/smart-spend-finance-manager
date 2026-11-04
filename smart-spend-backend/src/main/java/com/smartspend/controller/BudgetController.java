package com.smartspend.controller;

import com.smartspend.model.dto.budget.BudgetRequest;
import com.smartspend.model.dto.budget.BudgetResponse;
import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Endpoints for managing monthly category spending budgets and thresholds")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "Get budgets for specified month and year (defaults to current month)")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        List<BudgetResponse> budgets = budgetService.getBudgets(userDetails.getId(), month, year);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @PostMapping
    @Operation(summary = "Set a new monthly category budget target")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing monthly budget limit")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing monthly budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        budgetService.deleteBudget(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
