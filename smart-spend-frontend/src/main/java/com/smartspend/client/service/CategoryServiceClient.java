package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.model.CategoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public List<CategoryItem> getCategories(String type) throws Exception {
        String endpoint = "/categories" + (type != null && !type.isBlank() ? "?type=" + type : "");
        Map<String, Object> response = apiClient.get(endpoint, new TypeReference<Map<String, Object>>() {});

        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, new TypeReference<List<CategoryItem>>() {});
        }
        return List.of();
    }

    public CategoryItem createCategory(String name, String type, String icon, String color) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("type", type);
        request.put("icon", icon != null ? icon : "tag");
        request.put("color", color != null ? color : "#3b82f6");

        Map<String, Object> response = apiClient.post("/categories", request, new TypeReference<Map<String, Object>>() {});
        if (response != null && response.containsKey("data")) {
            byte[] bytes = apiClient.getObjectMapper().writeValueAsBytes(response.get("data"));
            return apiClient.getObjectMapper().readValue(bytes, CategoryItem.class);
        }
        return null;
    }

    public void deleteCategory(Long categoryId) throws Exception {
        apiClient.delete("/categories/" + categoryId, new TypeReference<Map<String, Object>>() {});
    }
}
