package com.smartspend.controller;

import com.smartspend.model.dto.common.ApiResponse;
import com.smartspend.model.dto.portfolio.PortfolioSummaryResponse;
import com.smartspend.model.dto.portfolio.StockHoldingRequest;
import com.smartspend.model.dto.portfolio.StockHoldingResponse;
import com.smartspend.model.dto.portfolio.StockQuoteDto;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Stock Portfolio & Live Rates", description = "Endpoints for tracking Indian stock market investments with live NSE/BSE rates")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "Get current user's investment portfolio with live market rates & P&L calculations")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Portfolio retrieved successfully", summary));
    }

    @GetMapping("/quote/{symbol}")
    @Operation(summary = "Fetch live stock quote preview from Indian markets (NSE)")
    public ResponseEntity<ApiResponse<StockQuoteDto>> getLiveQuote(@PathVariable("symbol") String symbol) {
        StockQuoteDto quote = portfolioService.getQuote(symbol);
        return ResponseEntity.ok(ApiResponse.success("Quote fetched successfully", quote));
    }

    @PostMapping("/holdings")
    @Operation(summary = "Add a new stock investment holding")
    public ResponseEntity<ApiResponse<StockHoldingResponse>> addHolding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StockHoldingRequest request) {
        StockHoldingResponse response = portfolioService.addHolding(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock holding added successfully", response));
    }

    @PutMapping("/holdings/{id}")
    @Operation(summary = "Update an existing stock holding (Anti-IDOR enforced)")
    public ResponseEntity<ApiResponse<StockHoldingResponse>> updateHolding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @RequestBody StockHoldingRequest request) {
        StockHoldingResponse response = portfolioService.updateHolding(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Stock holding updated successfully", response));
    }

    @DeleteMapping("/holdings/{id}")
    @Operation(summary = "Delete a stock holding (Anti-IDOR enforced)")
    public ResponseEntity<ApiResponse<Void>> deleteHolding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {
        portfolioService.deleteHolding(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Stock holding deleted successfully", null));
    }
}
