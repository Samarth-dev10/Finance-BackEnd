package com.finance.core.constant;


public final class ErrorCodes {

    private ErrorCodes() { /* utility class — no instances */ }

    public static final String NOT_FOUND          = "NOT_FOUND";
    public static final String DUPLICATE_ENTRY    = "DUPLICATE_ENTRY";
    public static final String VALIDATION_ERROR   = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED        = "UNAUTHORIZED";
    public static final String FORBIDDEN           = "FORBIDDEN";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_INACTIVE    = "ACCOUNT_INACTIVE";
    public static final String INTERNAL_ERROR      = "INTERNAL_ERROR";
    public static final String INVALID_TOKEN       = "INVALID_TOKEN";
    public static final String OPERATION_NOT_ALLOWED = "OPERATION_NOT_ALLOWED";
}