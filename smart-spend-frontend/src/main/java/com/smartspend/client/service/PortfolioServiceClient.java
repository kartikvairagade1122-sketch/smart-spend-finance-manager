package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.PortfolioData;
import com.smartspend.client.model.StockHoldingItem;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PortfolioServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public PortfolioData getPortfolio() throws Exception {
        Map<String, Object> response = apiClient.get("/portfolio", new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, PortfolioData.class);
        }
        return new PortfolioData();
    }

    public Map<String, Object> getLiveQuote(String symbol) throws Exception {
        String encoded = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
        Map<String, Object> response = apiClient.get("/portfolio/quote/" + encoded, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> quote = (Map<String, Object>) response.get("data");
            return quote;
        }
        return null;
    }

    public StockHoldingItem addHolding(String symbol, String companyName, Integer quantity,
                                       BigDecimal buyPrice, LocalDate buyDate) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("symbol", symbol);
        request.put("companyName", companyName);
        request.put("quantity", quantity);
        request.put("buyPrice", buyPrice);
        request.put("buyDate", buyDate != null ? buyDate.toString() : LocalDate.now().toString());

        Map<String, Object> response = apiClient.post("/portfolio/holdings", request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, StockHoldingItem.class);
        }
        return null;
    }

    public StockHoldingItem updateHolding(Long id, String symbol, String companyName, Integer quantity,
                                          BigDecimal buyPrice, LocalDate buyDate) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("symbol", symbol);
        request.put("companyName", companyName);
        request.put("quantity", quantity);
        request.put("buyPrice", buyPrice);
        request.put("buyDate", buyDate != null ? buyDate.toString() : LocalDate.now().toString());

        Map<String, Object> response = apiClient.put("/portfolio/holdings/" + id, request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, StockHoldingItem.class);
        }
        return null;
    }

    public void deleteHolding(Long id) throws Exception {
        apiClient.delete("/portfolio/holdings/" + id, new TypeReference<Map<String, Object>>() {});
    }
}
