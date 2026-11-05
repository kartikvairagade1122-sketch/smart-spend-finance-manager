package com.smartspend.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartspend.client.util.CurrencyHelper;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockHoldingItem {

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

    public StockHoldingItem() {}

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

    // Formatted helper getters for JavaFX TableView
    public String getFormattedBuyPrice() {
        return CurrencyHelper.format(buyPrice);
    }

    public String getFormattedLivePrice() {
        return CurrencyHelper.format(livePrice);
    }

    public String getFormattedInvested() {
        return CurrencyHelper.format(totalInvested);
    }

    public String getFormattedCurrentValue() {
        return CurrencyHelper.format(currentValue);
    }

    public String getFormattedPnl() {
        if (pnlAmount == null) return "₹0.00 (0.00%)";
        String sign = pnlAmount.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + CurrencyHelper.format(pnlAmount) + " (" + (pnlPercentage != null ? pnlPercentage.toPlainString() : "0") + "%)";
    }

    public boolean isProfitable() {
        return pnlAmount != null && pnlAmount.compareTo(BigDecimal.ZERO) >= 0;
    }
}
