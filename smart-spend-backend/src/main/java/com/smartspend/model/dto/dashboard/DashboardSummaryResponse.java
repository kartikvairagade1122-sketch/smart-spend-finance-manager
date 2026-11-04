package com.smartspend.model.dto.dashboard;

import com.smartspend.model.dto.report.CategoryBreakdownDto;
import com.smartspend.model.dto.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryResponse {

    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal currentBalance = BigDecimal.ZERO;

    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    private BigDecimal monthlyExpenses = BigDecimal.ZERO;
    private BigDecimal monthlySavings = BigDecimal.ZERO;
    private double savingsRate = 0.0;

    private String currencyCode = "INR";
    private String currencySymbol = "₹";

    private List<TransactionResponse> recentTransactions = new ArrayList<>();
    private List<CategoryBreakdownDto> expenseCategoryBreakdown = new ArrayList<>();
    private List<MonthlyTrendDto> monthlyTrends = new ArrayList<>();

    public DashboardSummaryResponse() {}

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getMonthlySavings() {
        return monthlySavings;
    }

    public void setMonthlySavings(BigDecimal monthlySavings) {
        this.monthlySavings = monthlySavings;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<CategoryBreakdownDto> getExpenseCategoryBreakdown() {
        return expenseCategoryBreakdown;
    }

    public void setExpenseCategoryBreakdown(List<CategoryBreakdownDto> expenseCategoryBreakdown) {
        this.expenseCategoryBreakdown = expenseCategoryBreakdown;
    }

    public List<MonthlyTrendDto> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<MonthlyTrendDto> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
}
