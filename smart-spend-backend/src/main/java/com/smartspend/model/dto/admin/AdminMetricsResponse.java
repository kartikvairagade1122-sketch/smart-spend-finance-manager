package com.smartspend.model.dto.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminMetricsResponse {
    private long totalUsers;
    private long totalTransactions;
    private long totalStockHoldings;
    private BigDecimal totalCapitalTracked;
    private List<AdminUserSummaryDto> users = new ArrayList<>();

    public AdminMetricsResponse() {}

    public AdminMetricsResponse(long totalUsers, long totalTransactions, long totalStockHoldings,
                                BigDecimal totalCapitalTracked, List<AdminUserSummaryDto> users) {
        this.totalUsers = totalUsers;
        this.totalTransactions = totalTransactions;
        this.totalStockHoldings = totalStockHoldings;
        this.totalCapitalTracked = totalCapitalTracked;
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getTotalStockHoldings() {
        return totalStockHoldings;
    }

    public void setTotalStockHoldings(long totalStockHoldings) {
        this.totalStockHoldings = totalStockHoldings;
    }

    public BigDecimal getTotalCapitalTracked() {
        return totalCapitalTracked;
    }

    public void setTotalCapitalTracked(BigDecimal totalCapitalTracked) {
        this.totalCapitalTracked = totalCapitalTracked;
    }

    public List<AdminUserSummaryDto> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUserSummaryDto> users) {
        this.users = users;
    }
}
