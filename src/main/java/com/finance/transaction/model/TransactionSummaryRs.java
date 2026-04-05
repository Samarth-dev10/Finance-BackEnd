package com.finance.transaction.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionSummaryRs {

    private Long id;
    private BigDecimal amount;
    private String type;       // "INCOME" or "EXPENSE"
    private String category;
    private LocalDate date;
    private String notes;
}