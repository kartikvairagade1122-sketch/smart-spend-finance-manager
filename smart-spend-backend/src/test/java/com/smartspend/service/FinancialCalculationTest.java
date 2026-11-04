package com.smartspend.service;

import com.smartspend.model.dto.budget.BudgetResponse;
import com.smartspend.model.entity.Budget;
import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.BudgetRepository;
import com.smartspend.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialCalculationTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    @DisplayName("FINANCIAL: Accurate DECIMAL calculations without floating-point errors")
    void testFinancialBigDecimalPrecision() {
        // Floating point double 0.1 + 0.2 gives 0.30000000000000004
        // BigDecimal guarantees exact 0.30 in Indian Rupee transactions
        BigDecimal val1 = new BigDecimal("0.10");
        BigDecimal val2 = new BigDecimal("0.20");
        BigDecimal sum = val1.add(val2);

        assertEquals(new BigDecimal("0.30"), sum);

        BigDecimal income = new BigDecimal("50000.75");
        BigDecimal expense = new BigDecimal("15250.25");
        BigDecimal net = income.subtract(expense);

        assertEquals(new BigDecimal("34750.50"), net);
    }

    @Test
    @DisplayName("FINANCIAL: Budget status flags Warning (>=75%) and Exceeded (>=100%)")
    void testBudgetUtilizationStatus() {
        User user = new User("Kartik", "kartik@test.com", "kartik", "hash");
        user.setId(1L);

        Category foodCategory = new Category(null, "Food & Dining", TransactionType.EXPENSE, "utensils", "#f97316", true);
        foodCategory.setId(10L);

        // Monthly limit: ₹10,000.00
        Budget budget = new Budget(user, foodCategory, new BigDecimal("10000.00"), 5, 2026);
        budget.setId(1L);

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 5, 2026))
                .thenReturn(Collections.singletonList(budget));

        // Case 1: Spent ₹8,000.00 (80% -> WARNING)
        when(transactionRepository.sumAmountByUserIdAndCategoryAndDateRange(eq(1L), eq(10L), any(), any()))
                .thenReturn(new BigDecimal("8000.00"));

        List<BudgetResponse> responses = budgetService.getBudgets(1L, 5, 2026);
        assertEquals(1, responses.size());
        assertEquals("WARNING", responses.get(0).getStatus());
        assertEquals(80.0, responses.get(0).getPercentageUsed());
        assertEquals(new BigDecimal("2000.00"), responses.get(0).getRemainingAmount());

        // Case 2: Spent ₹11,500.00 (115% -> EXCEEDED)
        when(transactionRepository.sumAmountByUserIdAndCategoryAndDateRange(eq(1L), eq(10L), any(), any()))
                .thenReturn(new BigDecimal("11500.00"));

        List<BudgetResponse> exceededResponses = budgetService.getBudgets(1L, 5, 2026);
        assertEquals("EXCEEDED", exceededResponses.get(0).getStatus());
        assertEquals(115.0, exceededResponses.get(0).getPercentageUsed());
        assertEquals(BigDecimal.ZERO, exceededResponses.get(0).getRemainingAmount());
    }

    @Test
    @DisplayName("FINANCIAL: CSV export includes all transactions and Indian Rupee values")
    void testCsvExportFormat() {
        ReportService reportService = new ReportService(transactionRepository, transactionService);

        User user = new User("Kartik", "kartik@test.com", "kartik", "hash");
        user.setId(1L);

        Category salaryCategory = new Category(null, "Salary", TransactionType.INCOME, "briefcase", "#16a34a", true);
        Transaction t1 = new Transaction(user, salaryCategory, TransactionType.INCOME,
                new BigDecimal("75000.00"), "Monthly Salary", LocalDate.of(2026, 5, 1));
        t1.setId(101L);

        when(transactionRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Collections.singletonList(t1));

        byte[] csvBytes = reportService.exportReportToCsv(1L, "MONTHLY", null, null);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("Transaction ID,Date,Type,Category,Amount (INR),Description"));
        assertTrue(csvContent.contains("101,2026-05-01,INCOME,\"Salary\",75000.00,\"Monthly Salary\""));
    }
}
