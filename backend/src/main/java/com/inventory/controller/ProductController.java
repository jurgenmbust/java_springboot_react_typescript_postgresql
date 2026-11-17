package com.inventory.controller;
import com.inventory.dto.*;
import com.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** Handles HTTP requests for products. The service contains business rules. */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController {
    private final ProductService service;
    // Constructor injection makes dependencies explicit and easy to test.
    public ProductController(ProductService service) { this.service = service; }
    /** Lists products. */
    @GetMapping
    @Operation(summary = "List products")
    public PageResponse<ProductResponse> list(
        @RequestParam(required = false) String search, @RequestParam(required = false) Long categoryId,
        @RequestParam(defaultValue = "false") boolean lowStock, @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "desc") String direction) {
        return service.list(search, categoryId, lowStock, page, size, sort, direction);
    }
    /** Retrieves a product. @param id The record ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Get a product")
    public ProductResponse get(@PathVariable @Positive Long id) { return service.get(id); }
    /** Creates a product. @param request The validated fields. */
    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + result.id())).body(result);
    }
    /** Updates a product. @param id The record ID. @param request The validated fields. */
    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse update(@PathVariable @Positive Long id, @Valid @RequestBody ProductRequest request) { return service.update(id, request); }
    /** Deletes a product. @param id The record ID. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
