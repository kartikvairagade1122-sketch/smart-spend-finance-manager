package com.smartspend.model.dto.report;

import java.math.BigDecimal;

public class CategoryBreakdownDto {

    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private double percentage = 0.0;
    private long transactionCount = 0;

    public CategoryBreakdownDto() {}

    public CategoryBreakdownDto(String categoryName, String categoryColor, String categoryIcon,
                                BigDecimal totalAmount, double percentage, long transactionCount) {
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.categoryIcon = categoryIcon;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
