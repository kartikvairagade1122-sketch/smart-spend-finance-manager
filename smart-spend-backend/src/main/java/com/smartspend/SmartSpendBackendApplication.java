package com.smartspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SmartSpendBackendApplication {

    public static void main(String[] args) {
        // Enforce India Standard Time (IST) globally for accurate financial transaction logging
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(SmartSpendBackendApplication.class, args);
    }
}
