package com.smartspend.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartspend.client.config.AppConfig;
import com.smartspend.client.util.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public <T> T get(String endpoint, TypeReference<T> typeReference) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + endpoint))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET();

        attachAuthHeader(builder);
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(response, typeReference);
    }

    public <T> T post(String endpoint, Object body, TypeReference<T> typeReference) throws Exception {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        attachAuthHeader(builder);
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(response, typeReference);
    }

    public <T> T put(String endpoint, Object body, TypeReference<T> typeReference) throws Exception {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        attachAuthHeader(builder);
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(response, typeReference);
    }

    public <T> T delete(String endpoint, TypeReference<T> typeReference) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + endpoint))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .DELETE();

        attachAuthHeader(builder);
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return processResponse(response, typeReference);
    }

    public byte[] getBytes(String endpoint) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(30))
                .GET();

        attachAuthHeader(builder);
        HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() >= 400) {
            String errorMsg = new String(response.body());
            throw new IOException("Server returned HTTP " + response.statusCode() + ": " + errorMsg);
        }
        return response.body();
    }

    private void attachAuthHeader(HttpRequest.Builder builder) {
        String token = SessionManager.getInstance().getAccessToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    private <T> T processResponse(HttpResponse<String> response, TypeReference<T> typeReference) throws Exception {
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            if (body == null || body.isBlank()) {
                return null;
            }
            return objectMapper.readValue(body, typeReference);
        }

        // Parse standard error response message if available
        String errorMessage = "Server request failed (HTTP " + status + ")";
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("message")) {
                errorMessage = root.get("message").asText();
            } else if (root.has("error")) {
                errorMessage = root.get("error").asText();
            }
        } catch (Exception ignored) {}

        throw new IOException(errorMessage);
    }
}
