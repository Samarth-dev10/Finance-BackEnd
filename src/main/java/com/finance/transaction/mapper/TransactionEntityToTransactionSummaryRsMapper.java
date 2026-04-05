package com.finance.transaction.mapper;

import com.finance.core.entity.TransactionEntity;
import com.finance.transaction.model.TransactionSummaryRs;

import java.util.function.Function;


public class TransactionEntityToTransactionSummaryRsMapper
        implements Function<TransactionEntity, TransactionSummaryRs> {

    public static final TransactionEntityToTransactionSummaryRsMapper INSTANCE =
            new TransactionEntityToTransactionSummaryRsMapper();

    private TransactionEntityToTransactionSummaryRsMapper() {}

    @Override
    public TransactionSummaryRs apply(TransactionEntity entity) {
        if (entity == null) return null;

        return TransactionSummaryRs.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .type(entity.getType().name())
                .category(entity.getCategory())
                .date(entity.getDate())
                .notes(entity.getNotes())
                .build();
    }
}