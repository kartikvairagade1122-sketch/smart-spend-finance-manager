package com.smartspend.controller;

import com.smartspend.model.dto.admin.AdminMetricsResponse;
import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Operations", description = "Endpoints for platform owner metrics and user activity monitoring")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get platform metrics and user registry (Restricted to platform owner/admin)")
    public ResponseEntity<ApiResponse<AdminMetricsResponse>> getMetrics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AdminMetricsResponse metrics = adminService.getSystemMetrics(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Admin metrics retrieved successfully", metrics));
    }
}
