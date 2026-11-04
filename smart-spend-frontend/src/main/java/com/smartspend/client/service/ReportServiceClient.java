package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.ReportData;

import java.time.LocalDate;
import java.util.Map;

public class ReportServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public ReportData getReport(String period, LocalDate start, LocalDate end) throws Exception {
        StringBuilder sb = new StringBuilder("/reports/summary?period=").append(period != null ? period : "MONTHLY");
        if (start != null) sb.append("&startDate=").append(start);
        if (end != null) sb.append("&endDate=").append(end);

        Map<String, Object> response = apiClient.get(sb.toString(), new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, ReportData.class);
        }
        return new ReportData();
    }

    public byte[] downloadCsv(String period, LocalDate start, LocalDate end) throws Exception {
        StringBuilder sb = new StringBuilder("/reports/export/csv?period=").append(period != null ? period : "MONTHLY");
        if (start != null) sb.append("&startDate=").append(start);
        if (end != null) sb.append("&endDate=").append(end);

        return apiClient.getBytes(sb.toString());
    }
}
