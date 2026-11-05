package com.smartspend.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioData {

    private BigDecimal totalInvested = BigDecimal.ZERO;
    private BigDecimal currentValue = BigDecimal.ZERO;
    private BigDecimal totalPnl = BigDecimal.ZERO;
    private BigDecimal totalPnlPercentage = BigDecimal.ZERO;
    private Integer holdingsCount = 0;
    private Integer profitableCount = 0;
    private Integer lossCount = 0;
    private List<StockHoldingItem> holdings = new ArrayList<>();
    private String currency = "INR";

    public PortfolioData() {}

    public BigDecimal getTotalInvested() {
        return totalInvested != null ? totalInvested : BigDecimal.ZERO;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public BigDecimal getCurrentValue() {
        return currentValue != null ? currentValue : BigDecimal.ZERO;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getTotalPnl() {
        return totalPnl != null ? totalPnl : BigDecimal.ZERO;
    }

    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }

    public BigDecimal getTotalPnlPercentage() {
        return totalPnlPercentage != null ? totalPnlPercentage : BigDecimal.ZERO;
    }

    public void setTotalPnlPercentage(BigDecimal totalPnlPercentage) {
        this.totalPnlPercentage = totalPnlPercentage;
    }

    public Integer getHoldingsCount() {
        return holdingsCount != null ? holdingsCount : 0;
    }

    public void setHoldingsCount(Integer holdingsCount) {
        this.holdingsCount = holdingsCount;
    }

    public Integer getProfitableCount() {
        return profitableCount != null ? profitableCount : 0;
    }

    public void setProfitableCount(Integer profitableCount) {
        this.profitableCount = profitableCount;
    }

    public Integer getLossCount() {
        return lossCount != null ? lossCount : 0;
    }

    public void setLossCount(Integer lossCount) {
        this.lossCount = lossCount;
    }

    public List<StockHoldingItem> getHoldings() {
        return holdings != null ? holdings : new ArrayList<>();
    }

    public void setHoldings(List<StockHoldingItem> holdings) {
        this.holdings = holdings;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
