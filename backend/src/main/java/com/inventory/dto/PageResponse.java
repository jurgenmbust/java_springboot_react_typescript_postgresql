package com.inventory.dto;
import org.springframework.data.domain.Page;
import java.util.List;
/** Provides a stable pagination format. Pages start at zero. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> from(Page<T> result) {
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
