package com.smartspend.service;

import com.smartspend.model.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.model.dto.dashboard.MonthlyTrendDto;
import com.smartspend.model.dto.report.CategoryBreakdownDto;
import com.smartspend.model.dto.transaction.TransactionResponse;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public DashboardService(TransactionRepository transactionRepository,
                            TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        DashboardSummaryResponse response = new DashboardSummaryResponse();

        // 1. Overall Balance Totals
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setCurrentBalance(currentBalance);

        // 2. Current Month Calculations
        LocalDate now = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(now);
        LocalDate startOfMonth = currentYearMonth.atDay(1);
        LocalDate endOfMonth = currentYearMonth.atEndOfMonth();

        BigDecimal monthlyIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, startOfMonth, endOfMonth);
        BigDecimal monthlyExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, startOfMonth, endOfMonth);

        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;
        if (monthlyExpenses == null) monthlyExpenses = BigDecimal.ZERO;

        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        double savingsRate = 0.0;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = monthlySavings.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        response.setMonthlyIncome(monthlyIncome);
        response.setMonthlyExpenses(monthlyExpenses);
        response.setMonthlySavings(monthlySavings);
        response.setSavingsRate(Math.round(savingsRate * 10.0) / 10.0);
        response.setCurrencySymbol("₹");
        response.setCurrencyCode("INR");

        // 3. Recent 5 Transactions
        List<TransactionResponse> recent = transactionRepository.findTop5ByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream()
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());
        response.setRecentTransactions(recent);

        // 4. Current Month Expense Category Breakdown
        List<Transaction> monthExpenses = transactionRepository.findByUserIdAndDateRange(userId, startOfMonth, endOfMonth)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        Map<String, BigDecimal> categorySums = new HashMap<>();
        Map<String, String> categoryColors = new HashMap<>();
        Map<String, String> categoryIcons = new HashMap<>();
        Map<String, Long> categoryCounts = new HashMap<>();

        for (Transaction t : monthExpenses) {
            String catName = t.getCategory() != null ? t.getCategory().getName() : "Other Expense";
            String catColor = t.getCategory() != null ? t.getCategory().getColor() : "#64748b";
            String catIcon = t.getCategory() != null ? t.getCategory().getIcon() : "tag";

            categorySums.merge(catName, t.getAmount(), BigDecimal::add);
            categoryColors.putIfAbsent(catName, catColor);
            categoryIcons.putIfAbsent(catName, catIcon);
            categoryCounts.merge(catName, 1L, Long::sum);
        }

        List<CategoryBreakdownDto> breakdownList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categorySums.entrySet()) {
            double pct = 0.0;
            if (monthlyExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = entry.getValue().divide(monthlyExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            breakdownList.add(new CategoryBreakdownDto(
                    entry.getKey(),
                    categoryColors.get(entry.getKey()),
                    categoryIcons.get(entry.getKey()),
                    entry.getValue(),
                    Math.round(pct * 10.0) / 10.0,
                    categoryCounts.get(entry.getKey())
            ));
        }
        breakdownList.sort(Comparator.comparing(CategoryBreakdownDto::getTotalAmount).reversed());
        response.setExpenseCategoryBreakdown(breakdownList);

        // 5. Past 6 Months Trends
        List<MonthlyTrendDto> trends = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYearMonth.minusMonths(i);
            LocalDate mStart = ym.atDay(1);
            LocalDate mEnd = ym.atEndOfMonth();

            BigDecimal inc = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, TransactionType.INCOME, mStart, mEnd);
            BigDecimal exp = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, TransactionType.EXPENSE, mStart, mEnd);

            String monthLabel = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            trends.add(new MonthlyTrendDto(
                    monthLabel,
                    ym.getMonthValue(),
                    ym.getYear(),
                    inc != null ? inc : BigDecimal.ZERO,
                    exp != null ? exp : BigDecimal.ZERO
            ));
        }
        response.setMonthlyTrends(trends);

        return response;
    }
}
