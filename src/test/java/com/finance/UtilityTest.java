package com.finance;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.AuditableEntity;
import com.finance.core.utility.Utility;
import com.finance.model.FinError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UtilityTest — tests the shared cross-cutting helpers.
 *
 * Pure unit tests — no Spring context, no mocks needed.
 * Utility is a static utility class so we can test it directly.
 */
@DisplayName("Utility Helper Tests")
class UtilityTest {

    // ── createError ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createError() — builds FinError with all fields set")
    void createError_allFields() {
        FinError error = Utility.createError(
                ErrorCodes.NOT_FOUND, "id", "Record not found");

        assertThat(error.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
        assertThat(error.getField()).isEqualTo("id");
        assertThat(error.getMessage()).isEqualTo("Record not found");
    }

    @Test
    @DisplayName("createError() — without field: field is null")
    void createError_withoutField() {
        FinError error = Utility.createError(ErrorCodes.INTERNAL_ERROR, "Something broke");

        assertThat(error.getCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
        assertThat(error.getField()).isNull();
        assertThat(error.getMessage()).isEqualTo("Something broke");
    }

    @Test
    @DisplayName("internalError() — produces INTERNAL_ERROR code")
    void internalError_producesCorrectCode() {
        FinError error = Utility.internalError("Unexpected failure");

        assertThat(error.getCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
        assertThat(error.getMessage()).isEqualTo("Unexpected failure");
    }


    @Test
    @DisplayName("applyIfNotNull() — applies setter when value is not null")
    void applyIfNotNull_appliesWhenNotNull() {
        AtomicReference<String> captured = new AtomicReference<>("original");

        Utility.applyIfNotNull("new value", captured::set);

        assertThat(captured.get()).isEqualTo("new value");
    }

    @Test
    @DisplayName("applyIfNotNull() — does NOT call setter when value is null")
    void applyIfNotNull_skipsWhenNull() {
        AtomicReference<String> captured = new AtomicReference<>("original");

        Utility.applyIfNotNull(null, captured::set);

        // Original value should be unchanged
        assertThat(captured.get()).isEqualTo("original");
    }


    @Test
    @DisplayName("setAuditFields() — sets createdBy on new entity (createdBy was null)")
    void setAuditFields_setsCreatedByForNewEntity() {
        // Use a concrete anonymous subclass of AuditableEntity for testing
        AuditableEntity entity = createBlankEntity();

        // createdBy is null → this is a new entity → should set createdBy
        Utility.setAuditFields(entity);

        // In a non-Spring context, getCurrentUsername() returns "system"
        assertThat(entity.getCreatedBy()).isEqualTo("system");
        assertThat(entity.getUpdatedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("setAuditFields() — does NOT override createdBy on existing entity")
    void setAuditFields_doesNotOverrideExistingCreatedBy() {
        AuditableEntity entity = createBlankEntity();
        entity.setCreatedBy("original_creator");

        // Entity already has a createdBy → should preserve it
        Utility.setAuditFields(entity);

        assertThat(entity.getCreatedBy()).isEqualTo("original_creator");
        assertThat(entity.getUpdatedBy()).isEqualTo("system"); // updatedBy always refreshed
    }


    /**
     * Creates a blank anonymous subclass of AuditableEntity for testing.
     * We cannot instantiate AuditableEntity directly because it's abstract.
     */
    private AuditableEntity createBlankEntity() {
        return new AuditableEntity() {};
    }
}