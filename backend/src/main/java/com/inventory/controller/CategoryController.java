package com.inventory.controller;
import com.inventory.dto.*;
import com.inventory.service.CategoryService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** Handles HTTP requests for categories. The service contains business rules. */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
public class CategoryController {
    private final CategoryService service;
    // Constructor injection makes dependencies explicit and easy to test.
    public CategoryController(CategoryService service) { this.service = service; }
    /** Lists categories. */
    @GetMapping
    @Operation(summary = "List categories")
    public java.util.List<CategoryResponse> list() { return service.list(); }
    /** Retrieves a category. @param id The record ID. */
    @GetMapping("/{id}")
    @Operation(summary = "Get a category")
    public CategoryResponse get(@PathVariable @Positive Long id) { return service.get(id); }
    /** Creates a category. @param request The validated fields. */
    @PostMapping
    @Operation(summary = "Create a category")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/categories/" + result.id())).body(result);
    }
    /** Updates a category. @param id The record ID. @param request The validated fields. */
    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public CategoryResponse update(@PathVariable @Positive Long id, @Valid @RequestBody CategoryRequest request) { return service.update(id, request); }
    /** Deletes a category. @param id The record ID. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
