package com.smartspend.repository;

import com.smartspend.model.entity.Category;
import com.smartspend.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isDefault = true OR (c.user.id = :userId) ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.isDefault = true OR c.user.id = :userId) AND c.type = :type ORDER BY c.name ASC")
    List<Category> findAvailableForUserAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.isDefault = true OR c.user.id = :userId)")
    Optional<Category> findByIdAndAvailableForUser(@Param("id") Long id, @Param("userId") Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByIsDefaultTrueAndNameIgnoreCase(String name);
}
