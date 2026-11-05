package com.smartspend.service;

import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.portfolio.PortfolioSummaryResponse;
import com.smartspend.model.dto.portfolio.StockHoldingRequest;
import com.smartspend.model.dto.portfolio.StockHoldingResponse;
import com.smartspend.model.dto.portfolio.StockQuoteDto;
import com.smartspend.model.entity.StockHolding;
import com.smartspend.model.entity.User;
import com.smartspend.repository.StockHoldingRepository;
import com.smartspend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private StockHoldingRepository holdingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private PortfolioService portfolioService;

    private User user;
    private StockHolding holding;

    @BeforeEach
    void setUp() {
        user = new User("Kartik Vairagade", "kartik@example.com", "kartik", "hashedpass");
        user.setId(1L);

        holding = new StockHolding(user, "RELIANCE.NS", "Reliance Industries Ltd", 10, new BigDecimal("1200.00"), LocalDate.now());
        holding.setId(101L);
    }

    @Test
    @DisplayName("Should accurately calculate portfolio valuation and profit/loss in INR")
    void testPortfolioValuationCalculation() {
        when(holdingRepository.findAllByUserId(1L)).thenReturn(List.of(holding));

        // Live market price = Rs 1300.00 (Profit of Rs 100 per share = Rs 1000 total)
        StockQuoteDto mockQuote = new StockQuoteDto(
                "RELIANCE.NS", "Reliance Industries Ltd",
                new BigDecimal("1300.00"), new BigDecimal("1280.00"),
                new BigDecimal("20.00"), new BigDecimal("1.56"),
                "INR", true
        );
        when(stockPriceService.getQuote("RELIANCE.NS")).thenReturn(mockQuote);

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(1L);

        assertNotNull(summary);
        assertEquals(new BigDecimal("12000.00"), summary.getTotalInvested()); // 10 * 1200
        assertEquals(new BigDecimal("13000.00"), summary.getCurrentValue());  // 10 * 1300
        assertEquals(new BigDecimal("1000.00"), summary.getTotalPnl());       // 13000 - 12000
        assertEquals(new BigDecimal("8.33"), summary.getTotalPnlPercentage()); // 1000 / 12000 * 100
        assertEquals(1, summary.getHoldingsCount());
        assertEquals(1, summary.getProfitableCount());
        assertEquals(0, summary.getLossCount());
    }

    @Test
    @DisplayName("Should block cross-tenant stock deletion (Anti-IDOR)")
    void testAntiIdorOnStockDeletion() {
        Long attackerUserId = 999L;
        Long targetHoldingId = 101L;

        when(holdingRepository.findByIdAndUserId(targetHoldingId, attackerUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            portfolioService.deleteHolding(targetHoldingId, attackerUserId);
        });

        verify(holdingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should successfully add holding with live quote auto-fetch")
    void testAddStockHolding() {
        StockHoldingRequest request = new StockHoldingRequest(
                "TCS", null, 5, new BigDecimal("2400.00"), LocalDate.now()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        StockQuoteDto mockQuote = new StockQuoteDto(
                "TCS.NS", "Tata Consultancy Services Ltd",
                new BigDecimal("2500.00"), new BigDecimal("2450.00"),
                new BigDecimal("50.00"), new BigDecimal("2.04"),
                "INR", true
        );
        when(stockPriceService.getQuote("TCS.NS")).thenReturn(mockQuote);

        StockHolding savedHolding = new StockHolding(user, "TCS.NS", "Tata Consultancy Services Ltd", 5, new BigDecimal("2400.00"), LocalDate.now());
        savedHolding.setId(202L);

        when(holdingRepository.save(any(StockHolding.class))).thenReturn(savedHolding);

        StockHoldingResponse response = portfolioService.addHolding(1L, request);

        assertNotNull(response);
        assertEquals("TCS.NS", response.getSymbol());
        assertEquals(new BigDecimal("12000.00"), response.getTotalInvested()); // 5 * 2400
        assertEquals(new BigDecimal("12500.00"), response.getCurrentValue());  // 5 * 2500
        assertEquals(new BigDecimal("500.00"), response.getPnlAmount());       // 12500 - 12000
    }
}
