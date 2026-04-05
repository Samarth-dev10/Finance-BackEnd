package com.finance.transaction.validator;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.validator.BaseValidator;
import com.finance.transaction.model.TransactionCreateRq;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;


@Component
public class TransactionValidator extends BaseValidator {

    private static final int MAX_YEARS_IN_PAST = 10;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    @Override
    public void validate(Object request) {
        reset();

        if (!(request instanceof TransactionCreateRq rq)) {
            addError(ErrorCodes.VALIDATION_ERROR, null, "Invalid request type for TransactionValidator");
            return;
        }

        // Rule 1: Amount sanity check
        if (rq.getAmount() != null && rq.getAmount().compareTo(MIN_AMOUNT) < 0) {
            addError(ErrorCodes.VALIDATION_ERROR, "amount",
                    "Amount must be at least 0.01");
        }

        // Rule 2: Date not too far in the past
        if (rq.getDate() != null) {
            LocalDate earliestAllowed = LocalDate.now().minusYears(MAX_YEARS_IN_PAST);
            if (rq.getDate().isBefore(earliestAllowed)) {
                addError(ErrorCodes.VALIDATION_ERROR, "date",
                        "Transaction date cannot be more than " + MAX_YEARS_IN_PAST + " years in the past");
            }
        }

        // Rule 3: Category cannot be blank or a test placeholder
        if (rq.getCategory() != null) {
            String cat = rq.getCategory().trim();
            if (cat.isBlank() || cat.equalsIgnoreCase("string") || cat.equalsIgnoreCase("test")) {
                addError(ErrorCodes.VALIDATION_ERROR, "category",
                        "Category must be a meaningful value (e.g., Salary, Rent, Food)");
            }
        }
    }
}