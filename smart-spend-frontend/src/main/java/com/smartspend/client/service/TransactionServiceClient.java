package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.TransactionItem;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public List<TransactionItem> getTransactions(String type, Long categoryId, LocalDate startDate,
                                                 LocalDate endDate, String query) throws Exception {
        StringBuilder sb = new StringBuilder("/transactions?page=0&size=200");
        if (type != null && !type.isBlank()) {
            sb.append("&type=").append(type);
        }
        if (categoryId != null) {
            sb.append("&categoryId=").append(categoryId);
        }
        if (startDate != null) {
            sb.append("&startDate=").append(startDate);
        }
        if (endDate != null) {
            sb.append("&endDate=").append(endDate);
        }
        if (query != null && !query.isBlank()) {
            sb.append("&query=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
        }

        Map<String, Object> response = apiClient.get(sb.toString(), new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pageData = (Map<String, Object>) response.get("data");
            if (pageData != null && pageData.containsKey("content")) {
                byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(pageData.get("content"));
                return apiClient.getObjectMapper().readValue(bytes, new TypeReference<List<TransactionItem>>() {});
            }
        }
        return List.of();
    }

    public TransactionItem createTransaction(Long categoryId, String type, BigDecimal amount,
                                             String description, LocalDate date) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("categoryId", categoryId);
        request.put("type", type);
        request.put("amount", amount);
        request.put("description", description);
        request.put("transactionDate", date != null ? date.toString() : LocalDate.now().toString());

        Map<String, Object> response = apiClient.post("/transactions", request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, TransactionItem.class);
        }
        return null;
    }

    public TransactionItem updateTransaction(Long id, Long categoryId, String type, BigDecimal amount,
                                             String description, LocalDate date) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("categoryId", categoryId);
        request.put("type", type);
        request.put("amount", amount);
        request.put("description", description);
        request.put("transactionDate", date != null ? date.toString() : LocalDate.now().toString());

        Map<String, Object> response = apiClient.put("/transactions/" + id, request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, TransactionItem.class);
        }
        return null;
    }

    public void deleteTransaction(Long id) throws Exception {
        apiClient.delete("/transactions/" + id, new TypeReference<Map<String, Object>>() {});
    }
}
