package com.finance.transaction.model;

import com.finance.core.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class TransactionEditRq {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    private TransactionType type;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}