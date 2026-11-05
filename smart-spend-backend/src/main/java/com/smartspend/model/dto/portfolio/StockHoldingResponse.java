package com.smartspend.model.dto.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockHoldingResponse {

    private Long id;
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal buyPrice;
    private LocalDate buyDate;
    private BigDecimal livePrice;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal pnlAmount;
    private BigDecimal pnlPercentage;
    private BigDecimal dayChangeAmount;
    private BigDecimal dayChangePercent;
    private String currency = "INR";

    public StockHoldingResponse() {}

    public StockHoldingResponse(Long id, String symbol, String companyName, Integer quantity, BigDecimal buyPrice,
                                LocalDate buyDate, BigDecimal livePrice, BigDecimal totalInvested,
                                BigDecimal currentValue, BigDecimal pnlAmount, BigDecimal pnlPercentage,
                                BigDecimal dayChangeAmount, BigDecimal dayChangePercent, String currency) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.buyDate = buyDate;
        this.livePrice = livePrice;
        this.totalInvested = totalInvested;
        this.currentValue = currentValue;
        this.pnlAmount = pnlAmount;
        this.pnlPercentage = pnlPercentage;
        this.dayChangeAmount = dayChangeAmount;
        this.dayChangePercent = dayChangePercent;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public LocalDate getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(LocalDate buyDate) {
        this.buyDate = buyDate;
    }

    public BigDecimal getLivePrice() {
        return livePrice;
    }

    public void setLivePrice(BigDecimal livePrice) {
        this.livePrice = livePrice;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getPnlAmount() {
        return pnlAmount;
    }

    public void setPnlAmount(BigDecimal pnlAmount) {
        this.pnlAmount = pnlAmount;
    }

    public BigDecimal getPnlPercentage() {
        return pnlPercentage;
    }

    public void setPnlPercentage(BigDecimal pnlPercentage) {
        this.pnlPercentage = pnlPercentage;
    }

    public BigDecimal getDayChangeAmount() {
        return dayChangeAmount;
    }

    public void setDayChangeAmount(BigDecimal dayChangeAmount) {
        this.dayChangeAmount = dayChangeAmount;
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
}
