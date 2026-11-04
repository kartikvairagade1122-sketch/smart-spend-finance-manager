package com.smartspend.service;

import com.smartspend.exception.InvalidOperationException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.transaction.TransactionRequest;
import com.smartspend.model.dto.transaction.TransactionResponse;
import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.Transaction;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              UserService userService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            String query,
            Pageable pageable) {

        return transactionRepository.findFilteredTransactions(userId, type, categoryId, startDate, endDate, query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userService.getUserOrThrow(userId);
        Category category = null;

        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryAvailableForUserOrThrow(request.getCategoryId(), userId);
            if (category.getType() != request.getType()) {
                throw new InvalidOperationException("Selected category type does not match transaction type " + request.getType());
            }
        }

        Transaction transaction = new Transaction(
                user,
                category,
                request.getType(),
                request.getAmount(),
                request.getDescription().trim(),
                request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now()
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryAvailableForUserOrThrow(request.getCategoryId(), userId);
            if (category.getType() != request.getType()) {
                throw new InvalidOperationException("Selected category type does not match transaction type " + request.getType());
            }
        }

        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription().trim());
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    public TransactionResponse mapToResponse(Transaction t) {
        if (t == null) return null;

        Long categoryId = null;
        String categoryName = "Uncategorized";
        String categoryColor = "#64748b";
        String categoryIcon = "tag";

        if (t.getCategory() != null) {
            categoryId = t.getCategory().getId();
            categoryName = t.getCategory().getName();
            categoryColor = t.getCategory().getColor();
            categoryIcon = t.getCategory().getIcon();
        }

        return new TransactionResponse(
                t.getId(),
                categoryId,
                categoryName,
                categoryColor,
                categoryIcon,
                t.getType(),
                t.getAmount(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getCreatedAt()
        );
    }
}
