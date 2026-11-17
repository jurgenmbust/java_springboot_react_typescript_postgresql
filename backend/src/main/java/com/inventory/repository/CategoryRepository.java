package com.inventory.repository;
import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
/** Spring Data generates CRUD queries and queries derived from method names. */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
