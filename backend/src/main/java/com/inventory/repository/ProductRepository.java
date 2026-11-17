package com.inventory.repository;
import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.*;
import java.math.BigDecimal;
/** Provides product queries, dynamic filters and inventory totals. */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsByCategoryId(Long categoryId);
    long countByQuantityLessThanEqual(int threshold);
    @Query("select coalesce(sum(p.price * p.quantity), 0) from Product p")
    BigDecimal inventoryValue();
}
