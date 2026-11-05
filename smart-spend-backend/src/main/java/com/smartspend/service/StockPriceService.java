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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedQuote> quoteCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_SECONDS = 60; // 1 minute in-memory cache

    // Common Indian Stock aliases and recent NSE ticker renames/demergers
    private static final Map<String, String> COMMON_ALIASES = Map.ofEntries(
            Map.entry("TATAMOTORS", "TMCV.NS"),
            Map.entry("TATA MOTORS", "TMCV.NS"),
            Map.entry("TATAMOTORS.NS", "TMCV.NS"),
            Map.entry("TATA MOTORS.NS", "TMCV.NS"),
            Map.entry("RELIENCE", "RELIANCE.NS"),
            Map.entry("RELIANCE", "RELIANCE.NS"),
            Map.entry("HDFCBANK", "HDFCBANK.NS"),
            Map.entry("HDFC BANK", "HDFCBANK.NS"),
            Map.entry("ICICIBANK", "ICICIBANK.NS"),
            Map.entry("ICICI BANK", "ICICIBANK.NS"),
            Map.entry("SBIN", "SBIN.NS"),
            Map.entry("SBI", "SBIN.NS"),
            Map.entry("INFY", "INFY.NS"),
            Map.entry("INFOSYS", "INFY.NS"),
            Map.entry("TCS", "TCS.NS"),
            Map.entry("WIPRO", "WIPRO.NS"),
            Map.entry("ITC", "ITC.NS"),
            Map.entry("BHARTIARTL", "BHARTIARTL.NS"),
            Map.entry("AIRTEL", "BHARTIARTL.NS")
    );

    public StockPriceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
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

        // 1. Try direct chart fetch
        StockQuoteDto quote = fetchChartQuote(symbol);
        if (quote != null && quote.isValid()) {
            quoteCache.put(symbol, new CachedQuote(quote, Instant.now()));
            return quote;
        }

        // 2. If chart fetch failed, attempt Yahoo search lookup (e.g. for renames or alternative tickers)
        String resolvedSymbol = searchIndianSymbol(rawSymbol);
        if (resolvedSymbol != null && !resolvedSymbol.equalsIgnoreCase(symbol)) {
            StockQuoteDto resolvedQuote = fetchChartQuote(resolvedSymbol);
            if (resolvedQuote != null && resolvedQuote.isValid()) {
                quoteCache.put(symbol, new CachedQuote(resolvedQuote, Instant.now()));
                return resolvedQuote;
            }
        }

        // Return fallback quote with valid = false
        return fallbackQuote(symbol, rawSymbol.trim().toUpperCase(Locale.ROOT), BigDecimal.ZERO);
    }

    private StockQuoteDto fetchChartQuote(String symbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" +
                    URLEncoder.encode(symbol, StandardCharsets.UTF_8) + "?interval=1d";
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
                    if (currentPriceDbl > 0.0) {
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

                        return new StockQuoteDto(
                                symbol,
                                companyName,
                                currentPrice,
                                prevClose,
                                change,
                                changePercent,
                                "INR",
                                true
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live quote for {}: {}", symbol, e.getMessage());
        }
        return null;
    }

    private String searchIndianSymbol(String query) {
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = "https://query1.finance.yahoo.com/v1/finance/search?q=" + encoded;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode quotes = root.path("quotes");
                if (quotes.isArray()) {
                    for (JsonNode q : quotes) {
                        String sym = q.path("symbol").asText("");
                        if (sym.endsWith(".NS") || sym.endsWith(".BO")) {
                            return sym;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Search fallback failed for query '{}': {}", query, e.getMessage());
        }
        return null;
    }

    public static String normalizeSymbol(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return "UNKNOWN";
        }
        String trimmed = rawSymbol.trim().toUpperCase(Locale.ROOT);
        if (COMMON_ALIASES.containsKey(trimmed)) {
            return COMMON_ALIASES.get(trimmed);
        }

        // Strip internal whitespace: e.g. "TATA MOTORS" -> "TATAMOTORS"
        String cleaned = trimmed.replaceAll("\\s+", "");
        if (COMMON_ALIASES.containsKey(cleaned)) {
            return COMMON_ALIASES.get(cleaned);
        }

        if (!cleaned.contains(".")) {
            // Default to National Stock Exchange of India (NSE)
            return cleaned + ".NS";
        }
        return cleaned;
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
                false,
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
