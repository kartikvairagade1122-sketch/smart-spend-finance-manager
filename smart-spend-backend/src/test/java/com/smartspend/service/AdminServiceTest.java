package com.smartspend.service;

import com.smartspend.exception.UnauthorizedAccessException;
import com.smartspend.model.dto.admin.AdminMetricsResponse;
import com.smartspend.model.entity.StockHolding;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.StockHoldingRepository;
import com.smartspend.repository.TransactionRepository;
import com.smartspend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockHoldingRepository stockHoldingRepository;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User("Kartik Owner", "kartik@smartspend.in", "kartik", "hash123");
        adminUser.setId(1L);

        normalUser = new User("Jane Doe", "jane@example.com", "janedoe", "hash456");
        normalUser.setId(2L);
    }

    @Test
    @DisplayName("Admin user can retrieve full system metrics and user summaries")
    void getSystemMetrics_asAdmin_succeeds() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(List.of(adminUser, normalUser));

        Transaction tx = new Transaction(adminUser, null, TransactionType.INCOME, BigDecimal.valueOf(50000), "Salary", LocalDate.now());
        when(transactionRepository.findAll()).thenReturn(List.of(tx));

        StockHolding holding = new StockHolding(adminUser, "INFY.NS", "Infosys", 10, BigDecimal.valueOf(1800), LocalDate.now());
        when(stockHoldingRepository.findAll()).thenReturn(List.of(holding));

        AdminMetricsResponse response = adminService.getSystemMetrics(1L);

        assertNotNull(response);
        assertEquals(2, response.getTotalUsers());
        assertEquals(1, response.getTotalTransactions());
        assertEquals(1, response.getTotalStockHoldings());
        // 50,000 + (10 * 1800 = 18000) = 68000
        assertEquals(BigDecimal.valueOf(68000), response.getTotalCapitalTracked());
        assertEquals(2, response.getUsers().size());
    }

    @Test
    @DisplayName("Regular non-admin user is rejected with UnauthorizedAccessException")
    void getSystemMetrics_asRegularUser_throwsUnauthorized() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        assertThrows(UnauthorizedAccessException.class, () -> {
            adminService.getSystemMetrics(2L);
        });

        verify(transactionRepository, never()).findAll();
        verify(stockHoldingRepository, never()).findAll();
    }
}
