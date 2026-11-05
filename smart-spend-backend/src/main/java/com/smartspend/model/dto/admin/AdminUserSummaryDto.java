package com.smartspend.model.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminUserSummaryDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private long transactionCount;
    private long stockCount;
    private BigDecimal totalInvested;
    private boolean active;

    public AdminUserSummaryDto() {}

    public AdminUserSummaryDto(Long id, String fullName, String username, String email,
                               LocalDateTime createdAt, long transactionCount, long stockCount,
                               BigDecimal totalInvested, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.transactionCount = transactionCount;
        this.stockCount = stockCount;
        this.totalInvested = totalInvested;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public long getStockCount() {
        return stockCount;
    }

    public void setStockCount(long stockCount) {
        this.stockCount = stockCount;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
