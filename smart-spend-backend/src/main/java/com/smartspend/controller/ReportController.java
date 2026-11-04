package com.smartspend.controller;

import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.model.dto.report.FinancialReportResponse;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints for financial reports, category analytics, and CSV exports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Generate financial report for specified period (DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM)")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> getReportSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "period", defaultValue = "MONTHLY") String period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinancialReportResponse report = reportService.generateReport(userDetails.getId(), period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export financial transactions to CSV format for authenticated user")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "period", defaultValue = "MONTHLY") String period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] csvData = reportService.exportReportToCsv(userDetails.getId(), period, startDate, endDate);
        String filename = "smartspend-report-" + period.toLowerCase() + "-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
