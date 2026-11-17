package com.inventory;
import com.inventory.entity.*;
import com.inventory.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** Runs Flyway, repositories, controllers and errors against real PostgreSQL. Docker is required. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class InventoryIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");
    @DynamicPropertySource static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @BeforeEach void clear() { products.deleteAll(); categories.deleteAll(); }
    private Category category() {
        Category c = new Category(); c.setName("Office"); c.setDescription(""); return categories.saveAndFlush(c);
    }
    private String body(Long categoryId, String sku, int quantity) {
        return "{\"name\":\"Keyboard\",\"description\":\"USB\",\"sku\":\"" + sku + "\",\"price\":\"19.99\",\"quantity\":" + quantity + ",\"categoryId\":" + categoryId + "}";
    }
    @Test void completeProductCrudAndFilters() throws Exception {
        Long cid = category().getId();
        mvc.perform(post("/api/products").contentType("application/json").content(body(cid, "KEY-1", 3)))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andExpect(jsonPath("$.sku").value("KEY-1"));
        Long id = products.findAll().getFirst().getId();
        mvc.perform(get("/api/products/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.price").value("19.99"));
        mvc.perform(get("/api/products").param("search", "key").param("categoryId", cid.toString()).param("lowStock", "true").param("sort", "price").param("direction", "asc").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].category.name").value("Office"));
        mvc.perform(get("/api/products").param("search", "%")).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/dashboard")).andExpect(status().isOk()).andExpect(jsonPath("$.totalInventoryValue").value("59.97")).andExpect(jsonPath("$.lowStockCount").value(1));
        mvc.perform(post("/api/products").contentType("application/json").content(body(cid, "key-1", 1))).andExpect(status().isConflict());
        mvc.perform(delete("/api/categories/{id}", cid)).andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
        mvc.perform(put("/api/products/{id}", id).contentType("application/json").content(body(cid, "KEY-2", 10))).andExpect(status().isOk());
        mvc.perform(get("/api/products").param("lowStock", "true")).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(delete("/api/products/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/categories/{id}", cid)).andExpect(status().isNoContent());
    }
    @Test void categoryCrudAndDuplicateName() throws Exception {
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"Tools\"}")).andExpect(status().isCreated());
        Long id = categories.findAll().getFirst().getId();
        mvc.perform(get("/api/categories")).andExpect(jsonPath("$[0].name").value("Tools"));
        mvc.perform(get("/api/categories/{id}", id)).andExpect(status().isOk());
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"tools\"}")).andExpect(status().isConflict());
        mvc.perform(put("/api/categories/{id}", id).contentType("application/json").content("{\"name\":\"Hardware\",\"description\":\"Parts\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Hardware"));
        mvc.perform(delete("/api/categories/{id}", id)).andExpect(status().isNoContent());
    }
    @Test void returnsStructuredValidationAndFilterErrors() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
        mvc.perform(get("/api/products").param("sort", "unknown")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/abc")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").contentType("application/json").content(body(999L, "NEW", 1))).andExpect(status().isNotFound());
    }
    @Test void rejectsInvalidIdsFractionalQuantityAndUnsupportedMethods() throws Exception {
        mvc.perform(get("/api/products/0")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/products")).andExpect(status().isMethodNotAllowed());
        String fractional = body(category().getId(), "FRACTION", 1).replace("\"quantity\":1", "\"quantity\":1.5");
        mvc.perform(post("/api/products").contentType("application/json").content(fractional)).andExpect(status().isBadRequest());
    }
    @Test void databaseRejectsDuplicateCategoryIgnoringCase() {
        category(); Category duplicate = new Category(); duplicate.setName("office"); duplicate.setDescription("");
        assertThrows(DataIntegrityViolationException.class, () -> categories.saveAndFlush(duplicate));
    }
    @Test void databaseRejectsNegativeStock() {
        Product p = new Product(); p.setName("Invalid"); p.setDescription(""); p.setSku("BAD"); p.setPrice(BigDecimal.ONE); p.setQuantity(-1); p.setCategory(category());
        assertThrows(DataIntegrityViolationException.class, () -> products.saveAndFlush(p));
    }
    @Test void openApiIncludesEndpointsAndErrorSchema() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/products'].get").exists())
            .andExpect(jsonPath("$.components.schemas.ApiError").exists());
    }
}
