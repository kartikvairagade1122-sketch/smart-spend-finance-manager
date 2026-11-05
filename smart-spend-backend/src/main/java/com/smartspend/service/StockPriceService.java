package com.smartspend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.model.dto.portfolio.StockQuoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedQuote> quoteCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_SECONDS = 60; // 1 minute in-memory cache

    public StockPriceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public StockQuoteDto getQuote(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return fallbackQuote("UNKNOWN", "Unknown Company", BigDecimal.ZERO);
        }

        String symbol = normalizeSymbol(rawSymbol);

        // Check cache
        CachedQuote cached = quoteCache.get(symbol);
        if (cached != null && cached.isValid(CACHE_TTL_SECONDS)) {
            return cached.quote;
        }

        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode resultNode = root.path("chart").path("result");

                if (resultNode.isArray() && resultNode.size() > 0) {
                    JsonNode meta = resultNode.get(0).path("meta");

                    double currentPriceDbl = meta.path("regularMarketPrice").asDouble(0.0);
                    double prevCloseDbl = meta.path("chartPreviousClose").asDouble(currentPriceDbl);
                    String companyName = meta.path("shortName").asText(
                            meta.path("longName").asText(symbol)
                    );

                    BigDecimal currentPrice = BigDecimal.valueOf(currentPriceDbl).setScale(2, RoundingMode.HALF_EVEN);
                    BigDecimal prevClose = BigDecimal.valueOf(prevCloseDbl).setScale(2, RoundingMode.HALF_EVEN);

                    BigDecimal change = currentPrice.subtract(prevClose);
                    BigDecimal changePercent = BigDecimal.ZERO;
                    if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                        changePercent = change.divide(prevClose, 4, RoundingMode.HALF_EVEN)
                                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
                    }

                    StockQuoteDto quote = new StockQuoteDto(
                            symbol,
                            companyName,
                            currentPrice,
                            prevClose,
                            change,
                            changePercent,
                            "INR",
                            true
                    );

                    quoteCache.put(symbol, new CachedQuote(quote, Instant.now()));
                    return quote;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live quote for {}: {}", symbol, e.getMessage());
        }

        // Return fallback if API call fails
        return fallbackQuote(symbol, symbol, BigDecimal.ZERO);
    }

    private String normalizeSymbol(String symbol) {
        String trimmed = symbol.trim().toUpperCase();
        if (!trimmed.contains(".")) {
            // Default to National Stock Exchange of India (NSE)
            return trimmed + ".NS";
        }
        return trimmed;
    }

    private StockQuoteDto fallbackQuote(String symbol, String name, BigDecimal defaultPrice) {
        return new StockQuoteDto(
                symbol,
                name,
                defaultPrice,
                defaultPrice,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "INR",
                false
        );
    }

    private static class CachedQuote {
        final StockQuoteDto quote;
        final Instant timestamp;

        CachedQuote(StockQuoteDto quote, Instant timestamp) {
            this.quote = quote;
            this.timestamp = timestamp;
        }

        boolean isValid(long ttlSeconds) {
            return Duration.between(timestamp, Instant.now()).getSeconds() < ttlSeconds;
        }
    }
}
