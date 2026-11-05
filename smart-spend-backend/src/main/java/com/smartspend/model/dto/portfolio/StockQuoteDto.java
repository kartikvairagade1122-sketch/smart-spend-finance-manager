package com.smartspend.model.dto.portfolio;

import java.math.BigDecimal;

public class StockQuoteDto {

    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    private String currency = "INR";
    private boolean marketOpen;
    private boolean valid = true;

    public StockQuoteDto() {}

    public StockQuoteDto(String symbol, String companyName, BigDecimal currentPrice, BigDecimal previousClose,
                         BigDecimal dayChange, BigDecimal dayChangePercent, String currency, boolean marketOpen) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.dayChange = dayChange;
        this.dayChangePercent = dayChangePercent;
        this.currency = currency;
        this.marketOpen = marketOpen;
        this.valid = currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public StockQuoteDto(String symbol, String companyName, BigDecimal currentPrice, BigDecimal previousClose,
                         BigDecimal dayChange, BigDecimal dayChangePercent, String currency, boolean marketOpen, boolean valid) {
        this(symbol, companyName, currentPrice, previousClose, dayChange, dayChangePercent, currency, marketOpen);
        this.valid = valid;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isMarketOpen() {
        return marketOpen;
    }

    public void setMarketOpen(boolean marketOpen) {
        this.marketOpen = marketOpen;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
