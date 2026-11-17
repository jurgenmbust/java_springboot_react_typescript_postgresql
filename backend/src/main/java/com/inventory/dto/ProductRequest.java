package com.inventory.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
/** Holds product input. BigDecimal keeps money precise. */
public record ProductRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 2000) String description,
    @NotBlank @Size(max = 64) @Pattern(regexp = "[a-zA-Z0-9_-]+") String sku,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal price,
    @NotNull @PositiveOrZero Integer quantity,
    @NotNull @Positive Long categoryId
) {}
