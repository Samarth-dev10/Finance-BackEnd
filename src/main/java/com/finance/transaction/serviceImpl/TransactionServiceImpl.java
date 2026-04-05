package com.finance.transaction.serviceImpl;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.TransactionEntity;
import com.finance.core.entity.UserEntity;
import com.finance.core.utility.Utility;
import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.transaction.mapper.TransactionEntityToTransactionRsMapper;
import com.finance.transaction.mapper.TransactionEntityToTransactionSummaryRsMapper;
import com.finance.transaction.model.*;
import com.finance.transaction.repository.TransactionRepository;
import com.finance.transaction.service.TransactionService;
import com.finance.transaction.validator.TransactionValidator;
import com.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository  transactionRepository;
    private final UserRepository         userRepository;
    private final TransactionValidator   transactionValidator;


    @Override
    @Transactional
    public FinResponse<TransactionRs> createTransaction(TransactionCreateRq createRq) {
        log.debug("Creating transaction: type={}, amount={}", createRq.getType(), createRq.getAmount());

        // Step 1: Business rule validation
        transactionValidator.validate(createRq);
        if (transactionValidator.hasErrors()) {
            return FinResponse.failure("Transaction creation failed", transactionValidator.getErrors());
        }

        // Step 2: Link the current authenticated user as the creator
        String currentUsername = Utility.getCurrentUsername();
        UserEntity currentUser = userRepository
                .findByUsernameAndIsActiveTrue(currentUsername)
                .orElse(null);

        if (currentUser == null) {
            return FinResponse.failure("Transaction creation failed",
                    Utility.createError(ErrorCodes.UNAUTHORIZED,
                            "Could not identify the current user"));
        }

        try {
            // Step 3: Build entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(createRq.getAmount());
            transaction.setType(createRq.getType());
            transaction.setCategory(createRq.getCategory().trim());
            transaction.setDate(createRq.getDate());
            transaction.setNotes(createRq.getNotes());
            transaction.setIsDeleted(false);
            transaction.setCreatedByUser(currentUser);

            // Step 4: Stamp audit fields
            Utility.setAuditFields(transaction);

            // Step 5: Persist and return
            TransactionEntity saved = transactionRepository.save(transaction);
            log.info("Transaction created: id={}, type={}, amount={}",
                    saved.getId(), saved.getType(), saved.getAmount());

            return FinResponse.success("Transaction created successfully",
                    TransactionEntityToTransactionRsMapper.INSTANCE.apply(saved));

        } catch (Exception ex) {
            log.error("Error creating transaction: {}", ex.getMessage(), ex);
            FinResponse<TransactionRs> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to create transaction"));
            return r;
        }
    }


    @Override
    public FinResponse<TransactionRs> getTransactionById(Long id) {
        log.debug("Fetching transaction id={}", id);

        return transactionRepository
                .findByIdAndIsDeletedFalseAndIsActiveTrue(id)
                .map(entity -> FinResponse.success(
                        "Transaction fetched successfully",
                        TransactionEntityToTransactionRsMapper.INSTANCE.apply(entity)))
                .orElseGet(() -> {
                    FinResponse<TransactionRs> r = new FinResponse<>();
                    r.addError(Utility.createError(ErrorCodes.NOT_FOUND,
                            "Transaction not found with id: " + id));
                    return r;
                });
    }


    @Override
    public FinResponse<PagedRs<TransactionSummaryRs>> getAllTransactions(
            TransactionFilterRq filterRq) {

        log.debug("Fetching transactions with filters: {}", filterRq);

        try {
            int safePage = Math.max(filterRq.getPage(), 0);
            int safeSize = filterRq.getSize() <= 0 ? 20 : Math.min(filterRq.getSize(), 100);

            var pageable = PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by(Sort.Order.desc("date"), Sort.Order.desc("createdAt"))
            );

            // Normalize text filters to lowercase so the repository can compare
            // against LOWER(column) without applying LOWER() to JDBC parameters
            // (some drivers/dialects may bind them in a way that breaks LOWER()).
            String normalizedCategory = filterRq.getCategory();
            if (normalizedCategory != null) {
                normalizedCategory = normalizedCategory.trim().toLowerCase();
            }

            String normalizedNotes = filterRq.getNotes();
            if (normalizedNotes != null) {
                normalizedNotes = normalizedNotes.trim().toLowerCase();
            }

            boolean noTextCategory = normalizedCategory == null || normalizedCategory.isBlank();
            boolean noTextNotes = normalizedNotes == null || normalizedNotes.isBlank();
            boolean noFilters = filterRq.getType() == null
                    && noTextCategory
                    && filterRq.getStartDate() == null
                    && filterRq.getEndDate() == null
                    && noTextNotes;

            boolean typeOnly = filterRq.getType() != null
                    && noTextCategory
                    && filterRq.getStartDate() == null
                    && filterRq.getEndDate() == null
                    && noTextNotes;

            Page<TransactionEntity> entityPage;
            if (noFilters) {
                entityPage = transactionRepository.findByIsDeletedFalseAndIsActiveTrue(pageable);
            } else if (typeOnly) {
                entityPage = transactionRepository.findByIsDeletedFalseAndIsActiveTrueAndType(filterRq.getType(), pageable);
            } else {
                entityPage = transactionRepository.findAllWithFilters(
                        filterRq.getType(),
                        noTextCategory ? null : normalizedCategory,
                        filterRq.getStartDate(),
                        filterRq.getEndDate(),
                        noTextNotes ? null : normalizedNotes,
                        pageable
                );
            }

            PagedRs<TransactionSummaryRs> pagedRs = PagedRs.<TransactionSummaryRs>builder()
                    .content(entityPage.getContent().stream()
                            .map(TransactionEntityToTransactionSummaryRsMapper.INSTANCE)
                            .toList())
                    .page(entityPage.getNumber())
                    .size(entityPage.getSize())
                    .totalElements(entityPage.getTotalElements())
                    .totalPages(entityPage.getTotalPages())
                    .last(entityPage.isLast())
                    .build();

            return FinResponse.success("Transactions fetched successfully", pagedRs);

        } catch (Exception ex) {
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.error("Error fetching transactions: {} (type={}) | rootCause: {} (type={})",
                    ex.getMessage(), ex.getClass().getName(),
                    root.getMessage(), root.getClass().getName(),
                    ex);
            FinResponse<PagedRs<TransactionSummaryRs>> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to fetch transactions"));
            return r;
        }
    }


    @Override
    @Transactional
    public FinResponse<TransactionRs> editTransaction(Long id, TransactionEditRq editRq) {
        log.debug("Editing transaction id={}", id);

        var txOpt = transactionRepository
                .findByIdAndIsDeletedFalseAndIsActiveTrue(id);

        if (txOpt.isEmpty()) {
            FinResponse<TransactionRs> r = new FinResponse<>();
            r.addError(Utility.createError(ErrorCodes.NOT_FOUND,
                    "Transaction not found with id: " + id));
            return r;
        }

        try {
            TransactionEntity transaction = txOpt.get();

            // Apply only non-null fields — PATCH semantics via Utility helper
            Utility.applyIfNotNull(editRq.getAmount(),   transaction::setAmount);
            Utility.applyIfNotNull(editRq.getType(),     transaction::setType);
            Utility.applyIfNotNull(editRq.getDate(),     transaction::setDate);
            Utility.applyIfNotNull(editRq.getNotes(),    transaction::setNotes);

            // Trim category if provided
            if (editRq.getCategory() != null) {
                transaction.setCategory(editRq.getCategory().trim());
            }

            Utility.setAuditFields(transaction);
            TransactionEntity saved = transactionRepository.save(transaction);

            return FinResponse.success("Transaction updated successfully",
                    TransactionEntityToTransactionRsMapper.INSTANCE.apply(saved));

        } catch (Exception ex) {
            log.error("Error editing transaction id={}: {}", id, ex.getMessage(), ex);
            FinResponse<TransactionRs> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to update transaction"));
            return r;
        }
    }


    @Override
    @Transactional
    public FinResponse<Void> deleteTransaction(Long id) {
        log.debug("Soft-deleting transaction id={}", id);

        var txOpt = transactionRepository
                .findByIdAndIsDeletedFalseAndIsActiveTrue(id);

        if (txOpt.isEmpty()) {
            FinResponse<Void> r = new FinResponse<>();
            r.addError(Utility.createError(ErrorCodes.NOT_FOUND,
                    "Transaction not found with id: " + id));
            return r;
        }

        try {
            TransactionEntity transaction = txOpt.get();
            transaction.setIsDeleted(true);     // soft delete flag
            transaction.setIsActive(false);
            Utility.setAuditFields(transaction);
            transactionRepository.save(transaction);

            log.info("Transaction soft-deleted: id={}", id);
            return FinResponse.success("Transaction deleted successfully");

        } catch (Exception ex) {
            log.error("Error deleting transaction id={}: {}", id, ex.getMessage(), ex);
            FinResponse<Void> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to delete transaction"));
            return r;
        }
    }
}