package com.smartspend.client.config;

public class AppConfig {

    public static final String API_BASE_URL = System.getProperty("smartspend.api.url", "http://localhost:8080/api");
    public static final String CURRENCY_SYMBOL = "₹";
    public static final String CURRENCY_CODE = "INR";
    public static final String APP_NAME = "Smart Spend";
    public static final String APP_TAGLINE = "Track your money. Control your future.";
}
