package com.inventory.service;
import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.exception.*;
import com.inventory.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Implements category rules. Spring injects the repositories through the constructor. */
@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    public CategoryServiceImpl(CategoryRepository categories, ProductRepository products) { this.categories = categories; this.products = products; }
    /** Lists categories alphabetically. */
    public List<CategoryResponse> list() { return categories.findAll(Sort.by("name")).stream().map(CategoryResponse::from).toList(); }
    /** Retrieves a category. @param id The category ID. */
    public CategoryResponse get(Long id) { return CategoryResponse.from(find(id)); }
    /** Creates a category in a write transaction. @param request The category fields. */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categories.existsByNameIgnoreCase(request.name().trim())) throw new ConflictException("A category with this name already exists.");
        Category category = new Category(); apply(category, request);
        return CategoryResponse.from(categories.saveAndFlush(category));
    }
    /** Updates a category. @param id The category ID. @param request The new fields. */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = find(id);
        if (categories.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) throw new ConflictException("A category with this name already exists.");
        apply(category, request); return CategoryResponse.from(categories.saveAndFlush(category));
    }
    /** Deletes an empty category. @param id The category ID. */
    @Transactional
    public void delete(Long id) {
        Category category = find(id);
        if (products.existsByCategoryId(id)) throw new ConflictException("Move or delete this category's products before deleting it.");
        categories.delete(category); categories.flush();
    }
    private Category find(Long id) { return categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found.")); }
    private void apply(Category category, CategoryRequest request) {
        category.setName(request.name().trim()); category.setDescription(request.description() == null ? "" : request.description().trim());
    }
}
