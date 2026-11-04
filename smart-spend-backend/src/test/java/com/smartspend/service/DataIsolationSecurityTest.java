package com.smartspend.service;

import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.dashboard.DashboardSummaryResponse;
import com.smartspend.model.dto.transaction.TransactionRequest;
import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataIsolationSecurityTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User userA;
    private User userB;
    private Transaction userBTransaction;

    @BeforeEach
    void setUp() {
        userA = new User("User A", "userA@test.com", "userA", "hashA");
        userA.setId(1L);

        userB = new User("User B", "userB@test.com", "userB", "hashB");
        userB.setId(2L);

        Category category = new Category(null, "Food", TransactionType.EXPENSE, "utensils", "#f97316", true);
        category.setId(10L);

        // Transaction #200 belongs strictly to User B
        userBTransaction = new Transaction(userB, category, TransactionType.EXPENSE,
                new BigDecimal("1500.00"), "User B Private Dinner", LocalDate.now());
        userBTransaction.setId(200L);
    }

    @Test
    @DisplayName("SECURITY: User A must NOT be able to read User B's transaction by ID")
    void testUserACannotReadUserBTransaction() {
        // When User A (ID: 1) requests transaction ID 200, repository searches WHERE id = 200 AND user_id = 1
        when(transactionRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById(1L, 200L);
        });

        verify(transactionRepository, times(1)).findByIdAndUserId(200L, 1L);
    }

    @Test
    @DisplayName("SECURITY: User A must NOT be able to edit User B's transaction")
    void testUserACannotEditUserBTransaction() {
        TransactionRequest updateReq = new TransactionRequest(
                10L, TransactionType.EXPENSE, new BigDecimal("9999.00"), "Tampered", LocalDate.now());

        when(transactionRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.updateTransaction(1L, 200L, updateReq);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("SECURITY: User A must NOT be able to delete User B's transaction")
    void testUserACannotDeleteUserBTransaction() {
        when(transactionRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(1L, 200L);
        });

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    @DisplayName("SECURITY: Dashboard calculations must strictly isolate User A from User B's financial figures")
    void testDashboardDataIsolation() {
        DashboardService dashboardService = new DashboardService(transactionRepository, transactionService);

        // User A's data: Income 50,000 INR, Expense 15,000 INR
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.INCOME))
                .thenReturn(new BigDecimal("50000.00"));
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("15000.00"));

        when(transactionRepository.findTop5ByUserIdOrderByTransactionDateDescIdDesc(1L))
                .thenReturn(Collections.emptyList());

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(1L);

        assertNotNull(summary);
        assertEquals(new BigDecimal("50000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("15000.00"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("35000.00"), summary.getCurrentBalance()); // ₹35,000 Net Balance
        assertEquals("₹", summary.getCurrencySymbol());

        // Verify that all calculations strictly used User A's ID (1L) and never touched User B (2L)
        verify(transactionRepository, times(1)).sumAmountByUserIdAndType(1L, TransactionType.INCOME);
        verify(transactionRepository, times(1)).sumAmountByUserIdAndType(1L, TransactionType.EXPENSE);
        verify(transactionRepository, never()).sumAmountByUserIdAndType(eq(2L), any());
    }
}
