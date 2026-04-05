package com.finance.transaction.mapper;

import com.finance.core.entity.TransactionEntity;
import com.finance.transaction.model.TransactionRs;

import java.util.function.Function;

public class TransactionEntityToTransactionRsMapper
        implements Function<TransactionEntity, TransactionRs> {

    public static final TransactionEntityToTransactionRsMapper INSTANCE =
            new TransactionEntityToTransactionRsMapper();

    private TransactionEntityToTransactionRsMapper() {}

    @Override
    public TransactionRs apply(TransactionEntity entity) {
        if (entity == null) return null;

        return TransactionRs.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .type(entity.getType().name())           // enum → String
                .category(entity.getCategory())
                .date(entity.getDate())
                .notes(entity.getNotes())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}