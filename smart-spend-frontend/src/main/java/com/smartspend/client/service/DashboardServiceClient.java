package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.DashboardData;

import java.util.Map;

public class DashboardServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public DashboardData getDashboardSummary() throws Exception {
        Map<String, Object> response = apiClient.get("/dashboard/summary", new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, DashboardData.class);
        }
        return new DashboardData();
    }
}
