package com.smartspend.service;

import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.InvalidOperationException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.budget.BudgetRequest;
import com.smartspend.model.dto.budget.BudgetResponse;
import com.smartspend.model.entity.Budget;
import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.BudgetRepository;
import com.smartspend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public BudgetService(BudgetRepository budgetRepository,
                         TransactionRepository transactionRepository,
                         CategoryService categoryService,
                         UserService userService) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, targetMonth, targetYear);
        return budgets.stream()
                .map(b -> mapToResponse(b, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.getCategoryId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException("A budget target has already been set for this category and month");
        }

        User user = userService.getUserOrThrow(userId);
        Category category = categoryService.getCategoryAvailableForUserOrThrow(request.getCategoryId(), userId);

        if (category.getType() != TransactionType.EXPENSE) {
            throw new InvalidOperationException("Budgets can only be created for expense categories");
        }

        Budget budget = new Budget(
                user,
                category,
                request.getMonthlyLimit(),
                request.getMonth(),
                request.getYear()
        );

        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved, userId);
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = categoryService.getCategoryAvailableForUserOrThrow(request.getCategoryId(), userId);
        if (category.getType() != TransactionType.EXPENSE) {
            throw new InvalidOperationException("Budgets can only be created for expense categories");
        }

        budget.setCategory(category);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        Budget updated = budgetRepository.save(budget);
        return mapToResponse(updated, userId);
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    private BudgetResponse mapToResponse(Budget budget, Long userId) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryAndDateRange(
                userId, budget.getCategory().getId(), start, end);

        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double percentage = 0.0;
        if (budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        String status = "NORMAL";
        if (percentage >= 100.0) {
            status = "EXCEEDED";
        } else if (percentage >= 75.0) {
            status = "WARNING";
        }

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getCategory().getColor(),
                budget.getCategory().getIcon(),
                budget.getMonthlyLimit(),
                spent,
                remaining,
                Math.round(percentage * 10.0) / 10.0,
                budget.getMonth(),
                budget.getYear(),
                status
        );
    }
}
