package com.inventory.service;
import com.inventory.dto.*;
/** Defines product use cases and their pagination options. */
public interface ProductService {
    PageResponse<ProductResponse> list(String search, Long categoryId, boolean lowStock, int page, int size, String sort, String direction);
    ProductResponse get(Long id);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
