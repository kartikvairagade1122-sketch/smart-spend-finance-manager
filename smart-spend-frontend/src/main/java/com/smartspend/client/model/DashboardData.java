package com.smartspend.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardData {

    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal currentBalance = BigDecimal.ZERO;

    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    private BigDecimal monthlyExpenses = BigDecimal.ZERO;
    private BigDecimal monthlySavings = BigDecimal.ZERO;
    private double savingsRate = 0.0;

    private String currencyCode = "INR";
    private String currencySymbol = "₹";

    private List<TransactionItem> recentTransactions = new ArrayList<>();
    private List<CategoryBreakdownItem> expenseCategoryBreakdown = new ArrayList<>();
    private List<MonthlyTrendItem> monthlyTrends = new ArrayList<>();

    public DashboardData() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryBreakdownItem {
        private String categoryName;
        private String categoryColor;
        private String categoryIcon;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private double percentage = 0.0;
        private long transactionCount = 0;

        public CategoryBreakdownItem() {}

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getCategoryColor() { return categoryColor; }
        public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
        public String getCategoryIcon() { return categoryIcon; }
        public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
        public long getTransactionCount() { return transactionCount; }
        public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonthlyTrendItem {
        private String monthName;
        private int month;
        private int year;
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;

        public MonthlyTrendItem() {}

        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }
        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }
    }

    // Getters and Setters
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
    public BigDecimal getMonthlySavings() { return monthlySavings; }
    public void setMonthlySavings(BigDecimal monthlySavings) { this.monthlySavings = monthlySavings; }
    public double getSavingsRate() { return savingsRate; }
    public void setSavingsRate(double savingsRate) { this.savingsRate = savingsRate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public List<TransactionItem> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionItem> recentTransactions) { this.recentTransactions = recentTransactions; }
    public List<CategoryBreakdownItem> getExpenseCategoryBreakdown() { return expenseCategoryBreakdown; }
    public void setExpenseCategoryBreakdown(List<CategoryBreakdownItem> expenseCategoryBreakdown) { this.expenseCategoryBreakdown = expenseCategoryBreakdown; }
    public List<MonthlyTrendItem> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendItem> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
}
