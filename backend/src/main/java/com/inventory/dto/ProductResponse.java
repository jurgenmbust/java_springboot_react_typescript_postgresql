package com.inventory.dto;
import com.inventory.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Returns product data. Money uses JSON strings to preserve decimal precision. */
public record ProductResponse(Long id, String name, String description, String sku,
    @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal price,
    Integer quantity, CategoryResponse category, LocalDateTime createdAt, LocalDateTime updatedAt) {
    /** Maps within a transaction so the lazy category can be read. @param product The stored product. */
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getSku(), product.getPrice(),
            product.getQuantity(), CategoryResponse.from(product.getCategory()), product.getCreatedAt(), product.getUpdatedAt());
    }
}
