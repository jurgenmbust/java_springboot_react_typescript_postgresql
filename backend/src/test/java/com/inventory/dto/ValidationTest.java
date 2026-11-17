package com.inventory.dto;
import jakarta.validation.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises Jakarta validation rules directly. */
class ValidationTest {
    @Test void rejectsInvalidProductFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var errors = factory.getValidator().validate(new ProductRequest(" ", "", "bad sku", new BigDecimal("-1.123"), -1, 0L));
            var fields = errors.stream().map(error -> error.getPropertyPath().toString()).toList();
            assertTrue(fields.containsAll(java.util.List.of("name", "sku", "price", "quantity", "categoryId")));
        }
    }
    @Test void acceptsZeroPriceAndStock() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(new ProductRequest("Item", null, "ITEM_1", BigDecimal.ZERO, 0, 1L)).isEmpty());
            assertFalse(factory.getValidator().validate(new CategoryRequest(" ", "")).isEmpty());
        }
    }
}
