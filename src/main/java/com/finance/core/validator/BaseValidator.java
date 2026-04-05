package com.finance.core.validator;

import com.finance.model.FinError;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseValidator {

    private final List<FinError> errors = new ArrayList<>();

    public abstract void validate(Object request);

    protected void addError(String code, String field, String message) {
        errors.add(FinError.builder()
                .code(code)
                .field(field)
                .message(message)
                .build());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<FinError> getErrors() {
        return new ArrayList<>(errors);  // defensive copy
    }

    public void reset() {
        errors.clear();
    }
}