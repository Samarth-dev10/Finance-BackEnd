package com.finance.core.enums;

/**
 * UserStatus — whether a user account is allowed to log in.
 *
 * ACTIVE   → can authenticate and use the API
 * INACTIVE → account is disabled; login will be rejected
 *
 * Using a status enum instead of a boolean gives us room to add
 * states like SUSPENDED or PENDING_VERIFICATION in the future.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}