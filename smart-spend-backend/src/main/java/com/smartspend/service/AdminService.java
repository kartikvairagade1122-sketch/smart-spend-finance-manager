package com.smartspend.service;

import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.exception.UnauthorizedAccessException;
import com.smartspend.model.dto.admin.AdminMetricsResponse;
import com.smartspend.model.dto.admin.AdminUserSummaryDto;
import com.smartspend.model.entity.StockHolding;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.User;
import com.smartspend.repository.StockHoldingRepository;
import com.smartspend.repository.TransactionRepository;
import com.smartspend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final StockHoldingRepository stockHoldingRepository;

    public AdminService(UserRepository userRepository,
                        TransactionRepository transactionRepository,
                        StockHoldingRepository stockHoldingRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.stockHoldingRepository = stockHoldingRepository;
    }

    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        String username = user.getUsername() != null ? user.getUsername().toLowerCase().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase().trim() : "";
        return username.equals("kartik")
                || username.equals("kartikdemo")
                || email.endsWith("@smartspend.in")
                || email.equals("kartikvairagade1122@gmail.com");
    }

    @Transactional(readOnly = true)
    public AdminMetricsResponse getSystemMetrics(Long requestingUserId) {
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestingUserId));

        if (!isAdmin(requester)) {
            throw new UnauthorizedAccessException("Access denied. Admin privileges required.");
        }

        List<User> allUsers = userRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<StockHolding> allHoldings = stockHoldingRepository.findAll();

        long totalUsers = allUsers.size();
        long totalTransactions = allTransactions.size();
        long totalStockHoldings = allHoldings.size();

        Map<Long, Long> txCountByUser = allTransactions.stream()
                .filter(t -> t.getUser() != null)
                .collect(Collectors.groupingBy(t -> t.getUser().getId(), Collectors.counting()));

        BigDecimal totalTxAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<StockHolding>> holdingsByUser = allHoldings.stream()
                .filter(h -> h.getUser() != null)
                .collect(Collectors.groupingBy(h -> h.getUser().getId()));

        BigDecimal totalStockInvested = BigDecimal.ZERO;
        for (StockHolding h : allHoldings) {
            if (h.getBuyPrice() != null && h.getQuantity() != null) {
                totalStockInvested = totalStockInvested.add(h.getBuyPrice().multiply(BigDecimal.valueOf(h.getQuantity())));
            }
        }

        BigDecimal totalCapitalTracked = totalTxAmount.add(totalStockInvested);

        List<AdminUserSummaryDto> userSummaries = new ArrayList<>();
        for (User u : allUsers) {
            long txCount = txCountByUser.getOrDefault(u.getId(), 0L);
            List<StockHolding> userHoldings = holdingsByUser.getOrDefault(u.getId(), List.of());
            long stockCount = userHoldings.size();
            BigDecimal userInvested = BigDecimal.ZERO;
            for (StockHolding sh : userHoldings) {
                if (sh.getBuyPrice() != null && sh.getQuantity() != null) {
                    userInvested = userInvested.add(sh.getBuyPrice().multiply(BigDecimal.valueOf(sh.getQuantity())));
                }
            }

            userSummaries.add(new AdminUserSummaryDto(
                    u.getId(),
                    u.getFullName(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getCreatedAt(),
                    txCount,
                    stockCount,
                    userInvested,
                    Boolean.TRUE.equals(u.getIsActive())
            ));
        }

        userSummaries.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L, a.getId() != null ? a.getId() : 0L));

        return new AdminMetricsResponse(
                totalUsers,
                totalTransactions,
                totalStockHoldings,
                totalCapitalTracked,
                userSummaries
        );
    }
}
