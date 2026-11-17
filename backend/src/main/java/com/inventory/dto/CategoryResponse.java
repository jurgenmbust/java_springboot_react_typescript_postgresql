package com.inventory.dto;
import com.inventory.entity.Category;
import java.time.LocalDateTime;
/** Returns category data without exposing the JPA entity. */
public record CategoryResponse(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
    /** Maps a category to JSON data. @param category The stored category. */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
