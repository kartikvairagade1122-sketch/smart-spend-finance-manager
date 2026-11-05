package com.smartspend.client;

/**
 * Entry point wrapper for JavaFX application execution.
 * Bypasses the JavaFX 11+ module-path verification requirement when running directly from IDEs.
 */
public class Launcher {
    public static void main(String[] args) {
        SmartSpendApp.main(args);
    }
}
