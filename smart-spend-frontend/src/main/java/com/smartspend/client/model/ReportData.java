package com.smartspend.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportData {

    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal netSavings = BigDecimal.ZERO;
    private double savingsRate = 0.0;
    private long totalTransactions = 0;
    private String currencySymbol = "₹";

    private List<DashboardData.CategoryBreakdownItem> categoryBreakdown = new ArrayList<>();
    private List<TransactionItem> transactions = new ArrayList<>();

    public ReportData() {}

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getNetSavings() { return netSavings; }
    public void setNetSavings(BigDecimal netSavings) { this.netSavings = netSavings; }
    public double getSavingsRate() { return savingsRate; }
    public void setSavingsRate(double savingsRate) { this.savingsRate = savingsRate; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public List<DashboardData.CategoryBreakdownItem> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<DashboardData.CategoryBreakdownItem> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public List<TransactionItem> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionItem> transactions) { this.transactions = transactions; }
}
