package com.smartspend.service;

import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.InvalidOperationException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.category.CategoryRequest;
import com.smartspend.model.dto.category.CategoryResponse;
import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.TransactionType;
import com.smartspend.model.entity.User;
import com.smartspend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesForUser(Long userId) {
        return categoryRepository.findAllAvailableForUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesForUserAndType(Long userId, TransactionType type) {
        return categoryRepository.findAvailableForUserAndType(userId, type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        String name = request.getName().trim();

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name) ||
            categoryRepository.existsByIsDefaultTrueAndNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A category with the name '" + name + "' already exists");
        }

        User user = userService.getUserOrThrow(userId);
        Category category = new Category(
                user,
                name,
                request.getType(),
                request.getIcon() != null ? request.getIcon().trim() : "tag",
                request.getColor() != null ? request.getColor().trim() : "#3b82f6",
                false
        );

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long userId, Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new InvalidOperationException("Default system categories cannot be modified");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        category.setName(request.getName().trim());
        category.setType(request.getType());
        if (request.getIcon() != null) category.setIcon(request.getIcon().trim());
        if (request.getColor() != null) category.setColor(request.getColor().trim());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new InvalidOperationException("Default system categories cannot be deleted");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        categoryRepository.delete(category);
    }

    public Category getCategoryAvailableForUserOrThrow(Long categoryId, Long userId) {
        if (categoryId == null) return null;
        return categoryRepository.findByIdAndAvailableForUser(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    public CategoryResponse mapToResponse(Category category) {
        if (category == null) return null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getIsDefault()
        );
    }
}
