package com.smartspend.client.util;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String fullName;
    private String username;
    private String email;
    private String currency = "INR";

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void setSession(String accessToken, String refreshToken, Long userId,
                                        String fullName, String username, String email, String currency) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.currency = (currency != null && !currency.isBlank()) ? currency : "INR";
    }

    public synchronized void clearSession() {
        this.accessToken = null;
        this.refreshToken = null;
        this.userId = null;
        this.fullName = null;
        this.username = null;
        this.email = null;
        this.currency = "INR";
    }

    public synchronized boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    public synchronized void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public synchronized Long getUserId() {
        return userId;
    }

    public synchronized String getFullName() {
        return fullName;
    }

    public synchronized void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public synchronized String getUsername() {
        return username;
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized String getCurrency() {
        return currency;
    }

    public synchronized void setCurrency(String currency) {
        this.currency = currency;
    }
}
