package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.BudgetItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public List<BudgetItem> getBudgets(Integer month, Integer year) throws Exception {
        StringBuilder sb = new StringBuilder("/budgets");
        if (month != null && year != null) {
            sb.append("?month=").append(month).append("&year=").append(year);
        }

        Map<String, Object> response = apiClient.get(sb.toString(), new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, new TypeReference<List<BudgetItem>>() {});
        }
        return List.of();
    }

    public BudgetItem createBudget(Long categoryId, BigDecimal monthlyLimit, Integer month, Integer year) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("categoryId", categoryId);
        request.put("monthlyLimit", monthlyLimit);
        request.put("month", month);
        request.put("year", year);

        Map<String, Object> response = apiClient.post("/budgets", request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, BudgetItem.class);
        }
        return null;
    }

    public void deleteBudget(Long id) throws Exception {
        apiClient.delete("/budgets/" + id, new TypeReference<Map<String, Object>>() {});
    }
}
