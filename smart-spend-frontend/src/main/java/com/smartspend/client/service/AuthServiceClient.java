package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smartspend.client.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class AuthServiceClient {

    private final ApiClient apiClient = ApiClient.getInstance();

    public boolean login(String usernameOrEmail, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("usernameOrEmail", usernameOrEmail);
        request.put("password", password);

        Map<String, Object> response = apiClient.post(
                "/auth/login",
                request,
                new TypeReference<Map<String, Object>>() {}
        );

        if (response != null && Boolean.TRUE.equals(response.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            String accessToken = (String) data.get("accessToken");
            String refreshToken = (String) data.get("refreshToken");

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) data.get("user");
            Long userId = ((Number) user.get("id")).longValue();
            String fullName = (String) user.get("fullName");
            String username = (String) user.get("username");
            String email = (String) user.get("email");
            String currency = (String) user.get("currency");

            SessionManager.getInstance().setSession(accessToken, refreshToken, userId, fullName, username, email, currency);
            return true;
        }
        return false;
    }

    public boolean register(String fullName, String email, String username, String password, String confirmPassword) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("fullName", fullName);
        request.put("email", email);
        request.put("username", username);
        request.put("password", password);
        request.put("confirmPassword", confirmPassword);

        Map<String, Object> response = apiClient.post(
                "/auth/register",
                request,
                new TypeReference<Map<String, Object>>() {}
        );

        if (response != null && Boolean.TRUE.equals(response.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            String accessToken = (String) data.get("accessToken");
            String refreshToken = (String) data.get("refreshToken");

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) data.get("user");
            Long userId = ((Number) user.get("id")).longValue();
            String userFullName = (String) user.get("fullName");
            String userName = (String) user.get("username");
            String userEmail = (String) user.get("email");
            String currency = (String) user.get("currency");

            SessionManager.getInstance().setSession(accessToken, refreshToken, userId, userFullName, userName, userEmail, currency);
            return true;
        }
        return false;
    }

    public void logout() {
        try {
            apiClient.post("/auth/logout", null, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {}
        SessionManager.getInstance().clearSession();
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", newPassword);
        request.put("confirmPassword", confirmPassword);

        apiClient.post("/auth/change-password", request, new TypeReference<Map<String, Object>>() {});
    }
}
