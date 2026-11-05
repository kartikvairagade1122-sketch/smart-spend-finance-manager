package com.smartspend.repository;

import com.smartspend.model.entity.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {

    @Query("SELECT s FROM StockHolding s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<StockHolding> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM StockHolding s WHERE s.id = :id AND s.user.id = :userId")
    Optional<StockHolding> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
