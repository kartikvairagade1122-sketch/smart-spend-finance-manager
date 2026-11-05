package com.smartspend.model.dto.portfolio;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioSummaryResponse {

    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercentage;
    private Integer holdingsCount;
    private Integer profitableCount;
    private Integer lossCount;
    private List<StockHoldingResponse> holdings;
    private String currency = "INR";

    public PortfolioSummaryResponse() {}

    public PortfolioSummaryResponse(BigDecimal totalInvested, BigDecimal currentValue, BigDecimal totalPnl,
                                    BigDecimal totalPnlPercentage, Integer holdingsCount, Integer profitableCount,
                                    Integer lossCount, List<StockHoldingResponse> holdings, String currency) {
        this.totalInvested = totalInvested;
        this.currentValue = currentValue;
        this.totalPnl = totalPnl;
        this.totalPnlPercentage = totalPnlPercentage;
        this.holdingsCount = holdingsCount;
        this.profitableCount = profitableCount;
        this.lossCount = lossCount;
        this.holdings = holdings;
        this.currency = currency;
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

    public BigDecimal getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }

    public BigDecimal getTotalPnlPercentage() {
        return totalPnlPercentage;
    }

    public void setTotalPnlPercentage(BigDecimal totalPnlPercentage) {
        this.totalPnlPercentage = totalPnlPercentage;
    }

    public Integer getHoldingsCount() {
        return holdingsCount;
    }

    public void setHoldingsCount(Integer holdingsCount) {
        this.holdingsCount = holdingsCount;
    }

    public Integer getProfitableCount() {
        return profitableCount;
    }

    public void setProfitableCount(Integer profitableCount) {
        this.profitableCount = profitableCount;
    }

    public Integer getLossCount() {
        return lossCount;
    }

    public void setLossCount(Integer lossCount) {
        this.lossCount = lossCount;
    }

    public List<StockHoldingResponse> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<StockHoldingResponse> holdings) {
        this.holdings = holdings;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
