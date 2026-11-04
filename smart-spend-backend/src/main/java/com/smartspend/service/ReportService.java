package com.smartspend.service;

import com.smartspend.model.dto.report.CategoryBreakdownDto;
import com.smartspend.model.dto.report.FinancialReportResponse;
import com.smartspend.model.dto.transaction.TransactionResponse;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public ReportService(TransactionRepository transactionRepository,
                         TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse generateReport(Long userId, String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(period, customStart, customEnd);
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        Map<String, BigDecimal> expenseCategoryMap = new HashMap<>();
        Map<String, String> categoryColors = new HashMap<>();
        Map<String, String> categoryIcons = new HashMap<>();
        Map<String, Long> categoryCounts = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());

                String catName = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                String catColor = t.getCategory() != null ? t.getCategory().getColor() : "#64748b";
                String catIcon = t.getCategory() != null ? t.getCategory().getIcon() : "tag";

                expenseCategoryMap.merge(catName, t.getAmount(), BigDecimal::add);
                categoryColors.putIfAbsent(catName, catColor);
                categoryIcons.putIfAbsent(catName, catIcon);
                categoryCounts.merge(catName, 1L, Long::sum);
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        List<CategoryBreakdownDto> breakdownList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : expenseCategoryMap.entrySet()) {
            double pct = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = entry.getValue().divide(totalExpenses, 4, RoundingMode.HALF_UP)
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

        List<TransactionResponse> txDtos = transactions.stream()
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());

        FinancialReportResponse response = new FinancialReportResponse();
        response.setPeriod(period != null ? period.toUpperCase() : "MONTHLY");
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(netSavings);
        response.setSavingsRate(Math.round(savingsRate * 10.0) / 10.0);
        response.setTotalTransactions(transactions.size());
        response.setCurrencySymbol("₹");
        response.setCategoryBreakdown(breakdownList);
        response.setTransactions(txDtos);

        return response;
    }

    @Transactional(readOnly = true)
    public byte[] exportReportToCsv(Long userId, String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate[] range = resolveDateRange(period, customStart, customEnd);
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, range[0], range[1]);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write CSV Header
            writer.println("Transaction ID,Date,Type,Category,Amount (INR),Description");

            for (Transaction t : transactions) {
                String cat = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                String safeDesc = t.getDescription().replace("\"", "\"\"");

                writer.printf("%d,%s,%s,\"%s\",%.2f,\"%s\"%n",
                        t.getId(),
                        t.getTransactionDate(),
                        t.getType(),
                        cat,
                        t.getAmount(),
                        safeDesc
                );
            }
        }

        return out.toByteArray();
    }

    private LocalDate[] resolveDateRange(String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate now = LocalDate.now();
        if ("DAILY".equalsIgnoreCase(period)) {
            return new LocalDate[]{now, now};
        } else if ("WEEKLY".equalsIgnoreCase(period)) {
            return new LocalDate[]{now.minusDays(6), now};
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            return new LocalDate[]{LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), 12, 31)};
        } else if ("CUSTOM".equalsIgnoreCase(period) && customStart != null && customEnd != null) {
            return new LocalDate[]{customStart, customEnd};
        } else {
            // Default to MONTHLY
            YearMonth ym = YearMonth.from(now);
            return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
        }
    }
}
