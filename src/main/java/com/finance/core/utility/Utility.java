package com.finance.core.utility;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.AuditableEntity;
import com.finance.model.FinError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility — cross-cutting helper methods used across all service layers.
 *
 * This class is intentionally NOT a Spring bean (@Component).
 * All methods are static so any class can call them without injection.
 *
 * Groups of helpers:
 *   1. Security context  → who is the current logged-in user?
 *   2. Audit fields      → stamp createdBy / updatedBy on entities
 *   3. Patch helpers     → apply non-null values (partial update / PATCH logic)
 *   4. Error factory     → create structured FinError objects consistently
 */
public final class Utility {

    private Utility() { /* no instances */ }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }


    public static void setAuditFields(AuditableEntity entity) {
        String currentUser = getCurrentUsername();
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(currentUser);
        }
        entity.setUpdatedBy(currentUser);
    }

    public static <T> void applyIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }


    public static FinError createError(String code, String field, String message) {
        return FinError.builder()
                .code(code)
                .field(field)
                .message(message)
                .build();
    }

    public static FinError createError(String code, String message) {
        return createError(code, null, message);
    }

    public static FinError internalError(String message) {
        return createError(ErrorCodes.INTERNAL_ERROR, message);
    }
}