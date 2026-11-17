package com.inventory.service;
import com.inventory.dto.*;
import com.inventory.entity.*;
import com.inventory.exception.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Applies product rules and keeps database work inside transactions. */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final int threshold;
    public ProductServiceImpl(ProductRepository products, CategoryRepository categories, @Value("${app.low-stock-threshold}") int threshold) {
        this.products = products; this.categories = categories;
        if (threshold < 0) throw new IllegalArgumentException("Stock threshold must be non-negative.");
        this.threshold = threshold;
    }
    /** Searches products. @param search Part of a name. @param categoryId Optional category. @param lowStock Whether to limit stock.
     * @param page Zero-based page. @param size Page size. @param sort Allowed field. @param direction asc or desc. */
    public PageResponse<ProductResponse> list(String search, Long categoryId, boolean lowStock, int page, int size, String sort, String direction) {
        if (page < 0 || size < 1 || size > 100 || !Set.of("name", "price", "createdAt").contains(sort)
            || !Set.of("asc", "desc").contains(direction) || (categoryId != null && categoryId <= 0) || (search != null && search.length() > 120)) {
            throw new IllegalArgumentException("Invalid pagination or filter.");
        }
        Specification<Product> filter = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            // Escape LIKE wildcards so input is treated as plain text.
            String escaped = search.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            filter = filter.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'));
        }
        if (categoryId != null) filter = filter.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        if (lowStock) filter = filter.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("quantity"), threshold));
        Sort order = Sort.by(Sort.Direction.fromString(direction), sort).and(Sort.by("id"));
        return PageResponse.from(products.findAll(filter, PageRequest.of(page, size, order)).map(ProductResponse::from));
    }
    /** Finds a product. @param id The product ID. */
    public ProductResponse get(Long id) { return ProductResponse.from(find(id)); }
    /** Creates a product after checking the SKU and category. @param request The product fields. */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (products.existsBySku(normalizeSku(request.sku()))) throw new ConflictException("A product with this SKU already exists.");
        Product product = new Product(); apply(product, request); return ProductResponse.from(products.saveAndFlush(product));
    }
    /** Updates a product. @param id The product ID. @param request The new fields. */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (products.existsBySkuAndIdNot(normalizeSku(request.sku()), id)) throw new ConflictException("A product with this SKU already exists.");
        apply(product, request); return ProductResponse.from(products.saveAndFlush(product));
    }
    /** Removes a product. @param id The product ID. */
    @Transactional
    public void delete(Long id) { products.delete(find(id)); products.flush(); }
    private Product find(Long id) { return products.findById(id).orElseThrow(() -> new NotFoundException("Product not found.")); }
    private String normalizeSku(String sku) { return sku.trim().toUpperCase(Locale.ROOT); }
    private void apply(Product product, ProductRequest request) {
        Category category = categories.findById(request.categoryId()).orElseThrow(() -> new NotFoundException("Category not found."));
        product.setName(request.name().trim()); product.setDescription(request.description() == null ? "" : request.description().trim());
        product.setSku(normalizeSku(request.sku())); product.setPrice(request.price()); product.setQuantity(request.quantity()); product.setCategory(category);
    }
}
