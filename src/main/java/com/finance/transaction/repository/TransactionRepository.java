package com.finance.transaction.repository;

import com.finance.core.entity.TransactionEntity;
import com.finance.core.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // =========================
    // BASIC FETCH
    // =========================
    Optional<TransactionEntity> findByIdAndIsDeletedFalseAndIsActiveTrue(Long id);

    Page<TransactionEntity> findByIsDeletedFalseAndIsActiveTrue(Pageable pageable);

    Page<TransactionEntity> findByIsDeletedFalseAndIsActiveTrueAndType(
            TransactionType type,
            Pageable pageable
    );

    // =========================
    // FILTERED SEARCH (FIXED)
    // =========================
    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
          AND (:type IS NULL OR t.type = :type)

          AND (
                :category IS NULL OR
                LOWER(t.category) LIKE LOWER(CONCAT('%', CAST(:category AS string), '%'))
              )

          AND t.date >= COALESCE(:startDate, t.date)
          AND t.date <= COALESCE(:endDate, t.date)

          AND (
                :notes IS NULL OR
                LOWER(COALESCE(t.notes, '')) LIKE LOWER(CONCAT('%', CAST(:notes AS string), '%'))
              )

        ORDER BY t.date DESC, t.createdAt DESC
    """)
    Page<TransactionEntity> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("notes") String notes,
            Pageable pageable
    );

    // =========================
    // TOTAL INCOME
    // =========================
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
          AND t.type      = 'INCOME'
          AND t.date >= COALESCE(:startDate, t.date)
          AND t.date <= COALESCE(:endDate, t.date)
    """)
    BigDecimal sumIncomeByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =========================
    // TOTAL EXPENSE
    // =========================
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
          AND t.type      = 'EXPENSE'
          AND t.date >= COALESCE(:startDate, t.date)
          AND t.date <= COALESCE(:endDate, t.date)
    """)
    BigDecimal sumExpenseByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =========================
    // CATEGORY BREAKDOWN
    // =========================
    @Query("""
        SELECT t.type, t.category, SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
          AND t.date >= COALESCE(:startDate, t.date)
          AND t.date <= COALESCE(:endDate, t.date)
        GROUP BY t.type, t.category
        ORDER BY SUM(t.amount) DESC
    """)
    List<Object[]> getCategoryTotals(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =========================
    // MONTHLY TRENDS
    // =========================
    @Query("""
        SELECT EXTRACT(YEAR FROM t.date),
               EXTRACT(MONTH FROM t.date),
               t.type,
               SUM(t.amount)
        FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
          AND t.date >= COALESCE(:startDate, t.date)
          AND t.date <= COALESCE(:endDate, t.date)
        GROUP BY EXTRACT(YEAR FROM t.date),
                 EXTRACT(MONTH FROM t.date),
                 t.type
        ORDER BY EXTRACT(YEAR FROM t.date),
                 EXTRACT(MONTH FROM t.date)
    """)
    List<Object[]> getMonthlyTrends(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =========================
    // COUNT
    // =========================
    @Query("""
        SELECT COUNT(t)
        FROM TransactionEntity t
        WHERE t.isDeleted = false
          AND t.isActive  = true
    """)
    long countActiveTransactions();
}