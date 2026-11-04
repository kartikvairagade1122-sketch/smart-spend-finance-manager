package com.smartspend.controller;

import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.model.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for aggregated financial KPIs, balances, trends, and charts")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get comprehensive financial dashboard summary for authenticated user")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
