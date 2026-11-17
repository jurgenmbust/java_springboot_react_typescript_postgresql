package com.inventory.service;
import com.inventory.dto.*;
import java.util.List;
/** Defines category operations independently from HTTP. */
public interface CategoryService {
    List<CategoryResponse> list();
    CategoryResponse get(Long id);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
