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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private final StockHoldingRepository holdingRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;

    public PortfolioService(StockHoldingRepository holdingRepository,
                            UserRepository userRepository,
                            StockPriceService stockPriceService) {
        this.holdingRepository = holdingRepository;
        this.userRepository = userRepository;
        this.stockPriceService = stockPriceService;
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        List<StockHolding> holdings = holdingRepository.findAllByUserId(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        int profitableCount = 0;
        int lossCount = 0;

        List<StockHoldingResponse> responseList = new ArrayList<>();

        for (StockHolding h : holdings) {
            StockQuoteDto quote = stockPriceService.getQuote(h.getSymbol());

            BigDecimal livePrice = quote.getCurrentPrice();
            if (livePrice.compareTo(BigDecimal.ZERO) <= 0) {
                // If live price unavailable, fallback to purchase price
                livePrice = h.getBuyPrice();
            }

            BigDecimal invested = h.getBuyPrice().multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal currentValue = livePrice.multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(2, RoundingMode.HALF_EVEN);

            BigDecimal pnl = currentValue.subtract(invested);
            BigDecimal pnlPercent = BigDecimal.ZERO;
            if (invested.compareTo(BigDecimal.ZERO) > 0) {
                pnlPercent = pnl.divide(invested, 4, RoundingMode.HALF_EVEN)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
            }

            BigDecimal dayChangeAmt = quote.getDayChange().multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(2, RoundingMode.HALF_EVEN);

            if (pnl.compareTo(BigDecimal.ZERO) >= 0) {
                profitableCount++;
            } else {
                lossCount++;
            }

            totalInvested = totalInvested.add(invested);
            totalCurrentValue = totalCurrentValue.add(currentValue);

            responseList.add(new StockHoldingResponse(
                    h.getId(),
                    h.getSymbol(),
                    h.getCompanyName(),
                    h.getQuantity(),
                    h.getBuyPrice(),
                    h.getBuyDate(),
                    livePrice,
                    invested,
                    currentValue,
                    pnl,
                    pnlPercent,
                    dayChangeAmt,
                    quote.getDayChangePercent(),
                    "INR"
            ));
        }

        BigDecimal totalPnl = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPnlPercent = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalPnlPercent = totalPnl.divide(totalInvested, 4, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
        }

        return new PortfolioSummaryResponse(
                totalInvested,
                totalCurrentValue,
                totalPnl,
                totalPnlPercent,
                holdings.size(),
                profitableCount,
                lossCount,
                responseList,
                "INR"
        );
    }

    @Transactional
    public StockHoldingResponse addHolding(Long userId, StockHoldingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String rawSymbol = request.getSymbol().trim().toUpperCase();
        String symbol = rawSymbol.contains(".") ? rawSymbol : rawSymbol + ".NS";

        // Auto-fetch company name from live quote if not supplied
        String companyName = request.getCompanyName();
        StockQuoteDto quote = stockPriceService.getQuote(symbol);
        if (companyName == null || companyName.trim().isEmpty()) {
            companyName = quote.getCompanyName();
        }

        LocalDate buyDate = request.getBuyDate() != null ? request.getBuyDate() : LocalDate.now();

        StockHolding holding = new StockHolding(
                user,
                symbol,
                companyName,
                request.getQuantity(),
                request.getBuyPrice(),
                buyDate
        );

        StockHolding saved = holdingRepository.save(holding);

        BigDecimal livePrice = quote.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0 ?
                quote.getCurrentPrice() : saved.getBuyPrice();

        BigDecimal invested = saved.getBuyPrice().multiply(BigDecimal.valueOf(saved.getQuantity()))
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal currentValue = livePrice.multiply(BigDecimal.valueOf(saved.getQuantity()))
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal pnl = currentValue.subtract(invested);
        BigDecimal pnlPercent = invested.compareTo(BigDecimal.ZERO) > 0 ?
                pnl.divide(invested, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN) :
                BigDecimal.ZERO;

        return new StockHoldingResponse(
                saved.getId(),
                saved.getSymbol(),
                saved.getCompanyName(),
                saved.getQuantity(),
                saved.getBuyPrice(),
                saved.getBuyDate(),
                livePrice,
                invested,
                currentValue,
                pnl,
                pnlPercent,
                BigDecimal.ZERO,
                quote.getDayChangePercent(),
                "INR"
        );
    }

    @Transactional
    public StockHoldingResponse updateHolding(Long id, Long userId, StockHoldingRequest request) {
        StockHolding holding = holdingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found with id: " + id));

        String rawSymbol = request.getSymbol().trim().toUpperCase();
        String symbol = rawSymbol.contains(".") ? rawSymbol : rawSymbol + ".NS";

        holding.setSymbol(symbol);
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            holding.setCompanyName(request.getCompanyName().trim());
        }
        holding.setQuantity(request.getQuantity());
        holding.setBuyPrice(request.getBuyPrice());
        if (request.getBuyDate() != null) {
            holding.setBuyDate(request.getBuyDate());
        }

        StockHolding updated = holdingRepository.save(holding);
        StockQuoteDto quote = stockPriceService.getQuote(updated.getSymbol());

        BigDecimal livePrice = quote.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0 ?
                quote.getCurrentPrice() : updated.getBuyPrice();

        BigDecimal invested = updated.getBuyPrice().multiply(BigDecimal.valueOf(updated.getQuantity()))
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal currentValue = livePrice.multiply(BigDecimal.valueOf(updated.getQuantity()))
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal pnl = currentValue.subtract(invested);
        BigDecimal pnlPercent = invested.compareTo(BigDecimal.ZERO) > 0 ?
                pnl.divide(invested, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN) :
                BigDecimal.ZERO;

        return new StockHoldingResponse(
                updated.getId(),
                updated.getSymbol(),
                updated.getCompanyName(),
                updated.getQuantity(),
                updated.getBuyPrice(),
                updated.getBuyDate(),
                livePrice,
                invested,
                currentValue,
                pnl,
                pnlPercent,
                BigDecimal.ZERO,
                quote.getDayChangePercent(),
                "INR"
        );
    }

    @Transactional
    public void deleteHolding(Long id, Long userId) {
        StockHolding holding = holdingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found with id: " + id));
        holdingRepository.delete(holding);
    }

    public StockQuoteDto getQuote(String symbol) {
        return stockPriceService.getQuote(symbol);
    }
}
