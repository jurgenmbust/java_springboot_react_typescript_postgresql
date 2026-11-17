package com.inventory.service;
import com.inventory.dto.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Reads dashboard totals from a consistent database snapshot. */
@Service
public class DashboardService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final ProductService productService;
    private final int threshold;
    public DashboardService(ProductRepository products, CategoryRepository categories, ProductService productService, @Value("${app.low-stock-threshold}") int threshold) {
        this.products = products; this.categories = categories; this.productService = productService; this.threshold = threshold;
    }
    /** Aggregates all records, independently of frontend pagination. */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DashboardResponse get() {
        return new DashboardResponse(products.count(), categories.count(), products.inventoryValue(), products.countByQuantityLessThanEqual(threshold), threshold,
            productService.list(null, null, false, 0, 5, "createdAt", "desc").content(), productService.list(null, null, true, 0, 5, "name", "asc").content());
    }
}
