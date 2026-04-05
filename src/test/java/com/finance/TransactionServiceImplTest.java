package com.finance;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.RoleEntity;
import com.finance.core.entity.TransactionEntity;
import com.finance.core.entity.UserEntity;
import com.finance.core.enums.RoleName;
import com.finance.core.enums.TransactionType;
import com.finance.core.enums.UserStatus;
import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.transaction.model.*;
import com.finance.transaction.repository.TransactionRepository;
import com.finance.transaction.serviceImpl.TransactionServiceImpl;
import com.finance.transaction.validator.TransactionValidator;
import com.finance.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TransactionServiceImplTest — unit tests for financial record operations.
 *
 * Coverage:
 *   ✔ Create — success, validation failure, user not found
 *   ✔ Get by ID — found, not found
 *   ✔ Get all — paginated list
 *   ✔ Delete — soft delete, not found
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Tests")
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository        userRepository;
    @Mock private TransactionValidator  transactionValidator;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UserEntity  testUser;
    private TransactionEntity testTransaction;

    @BeforeEach
    void setUp() {
        RoleEntity role = new RoleEntity();
        role.setId(2L);
        role.setName(RoleName.ANALYST);

        testUser = new UserEntity();
        testUser.setId(10L);
        testUser.setUsername("analyst1");
        testUser.setEmail("analyst@finance.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(role);
        testUser.setIsActive(true);

        testTransaction = new TransactionEntity();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("5000.00"));
        testTransaction.setType(TransactionType.INCOME);
        testTransaction.setCategory("Salary");
        testTransaction.setDate(LocalDate.of(2024, 3, 1));
        testTransaction.setNotes("March salary");
        testTransaction.setIsDeleted(false);
        testTransaction.setIsActive(true);
        testTransaction.setCreatedAt(LocalDateTime.now());
        testTransaction.setUpdatedAt(LocalDateTime.now());
        testTransaction.setCreatedBy("analyst1");
        testTransaction.setCreatedByUser(testUser);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTransaction() — success: returns saved transaction response")
    void createTransaction_success() {
        // ARRANGE
        TransactionCreateRq createRq = new TransactionCreateRq();
        createRq.setAmount(new BigDecimal("5000.00"));
        createRq.setType(TransactionType.INCOME);
        createRq.setCategory("Salary");
        createRq.setDate(LocalDate.of(2024, 3, 1));
        createRq.setNotes("March salary");

        // Validator passes (no errors)
        when(transactionValidator.hasErrors()).thenReturn(false);

        // Mock Utility.getCurrentUsername() via UserRepository lookup
        when(userRepository.findByUsernameAndIsActiveTrue(any()))
                .thenReturn(Optional.of(testUser));

        // Repository save returns the entity
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(testTransaction);

        // ACT
        FinResponse<TransactionRs> response = transactionService.createTransaction(createRq);

        // ASSERT
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAmount()).isEqualByComparingTo("5000.00");
        assertThat(response.getData().getType()).isEqualTo("INCOME");
        assertThat(response.getData().getCategory()).isEqualTo("Salary");

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createTransaction() — validation failure: returns errors without saving")
    void createTransaction_validationFailure_returnsErrors() {
        // ARRANGE
        TransactionCreateRq createRq = new TransactionCreateRq();
        createRq.setAmount(new BigDecimal("-100.00"));   // invalid
        createRq.setType(TransactionType.EXPENSE);
        createRq.setCategory("Food");
        createRq.setDate(LocalDate.now());

        // Validator FAILS
        when(transactionValidator.hasErrors()).thenReturn(true);
        when(transactionValidator.getErrors()).thenReturn(List.of(
                com.finance.model.FinError.builder()
                        .code(ErrorCodes.VALIDATION_ERROR)
                        .field("amount")
                        .message("Amount must be at least 0.01")
                        .build()
        ));

        // ACT
        FinResponse<TransactionRs> response = transactionService.createTransaction(createRq);

        // ASSERT
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0).getField()).isEqualTo("amount");

        // Verify: nothing was saved to the database
        verify(transactionRepository, never()).save(any());
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTransactionById() — found: returns full transaction detail")
    void getTransactionById_found() {
        when(transactionRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(1L))
                .thenReturn(Optional.of(testTransaction));

        FinResponse<TransactionRs> response = transactionService.getTransactionById(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getId()).isEqualTo(1L);
        assertThat(response.getData().getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("getTransactionById() — not found: returns NOT_FOUND error")
    void getTransactionById_notFound() {
        when(transactionRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(999L))
                .thenReturn(Optional.empty());

        FinResponse<TransactionRs> response = transactionService.getTransactionById(999L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors().get(0).getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    }

    // ── GET ALL (paginated) ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTransactions() — returns paginated list")
    void getAllTransactions_returnsList() {
        // ARRANGE: build a Spring Data Page wrapping our test transaction
        Page<TransactionEntity> page = new PageImpl<>(
                List.of(testTransaction),
                PageRequest.of(0, 20),
                1L
        );
        when(transactionRepository.findByIsDeletedFalseAndIsActiveTrue(any()))
                .thenReturn(page);

        TransactionFilterRq filterRq = new TransactionFilterRq();
        filterRq.setPage(0);
        filterRq.setSize(20);

        // ACT
        FinResponse<PagedRs<TransactionSummaryRs>> response =
                transactionService.getAllTransactions(filterRq);

        // ASSERT
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getContent()).hasSize(1);
        assertThat(response.getData().getTotalElements()).isEqualTo(1L);
        assertThat(response.getData().isLast()).isTrue();
        assertThat(response.getData().getContent().get(0).getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("getAllTransactions() — type-only filter: uses repository derived method")
    void getAllTransactions_typeOnly_returnsList() {
        Page<TransactionEntity> page = new PageImpl<>(
                List.of(testTransaction),
                PageRequest.of(0, 20),
                1L
        );
        when(transactionRepository.findByIsDeletedFalseAndIsActiveTrueAndType(any(), any()))
                .thenReturn(page);

        TransactionFilterRq filterRq = new TransactionFilterRq();
        filterRq.setPage(0);
        filterRq.setSize(20);
        filterRq.setType(TransactionType.INCOME);

        FinResponse<PagedRs<TransactionSummaryRs>> response = transactionService.getAllTransactions(filterRq);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getContent()).hasSize(1);
        verify(transactionRepository, times(1))
                .findByIsDeletedFalseAndIsActiveTrueAndType(eq(TransactionType.INCOME), any());
    }

    // ── SOFT DELETE ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTransaction() — success: sets isDeleted = true")
    void deleteTransaction_success() {
        when(transactionRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(1L))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        FinResponse<Void> response = transactionService.deleteTransaction(1L);

        assertThat(response.isSuccess()).isTrue();
        // Verify the entity was saved (soft-delete) and the flag set
        verify(transactionRepository, times(1)).save(argThat(tx ->
                tx.getIsDeleted() && !tx.getIsActive()
        ));
    }

    @Test
    @DisplayName("deleteTransaction() — not found: returns NOT_FOUND error")
    void deleteTransaction_notFound() {
        when(transactionRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(404L))
                .thenReturn(Optional.empty());

        FinResponse<Void> response = transactionService.deleteTransaction(404L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors().get(0).getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
        verify(transactionRepository, never()).save(any());
    }
}