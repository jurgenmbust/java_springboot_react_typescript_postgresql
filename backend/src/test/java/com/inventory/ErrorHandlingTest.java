package com.inventory;
import com.inventory.controller.ProductController;
import com.inventory.service.ProductService;
import com.inventory.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies that unexpected failures do not leak internal details. */
class ErrorHandlingTest {
    @Test void hidesInternalExceptionMessages() throws Exception {
        ProductService service = mock(ProductService.class);
        when(service.get(1L)).thenThrow(new RuntimeException("Private internal information"));
        MockMvcBuilders.standaloneSetup(new ProductController(service)).setControllerAdvice(new GlobalExceptionHandler()).build()
            .perform(get("/api/products/1")).andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected server error occurred."))
            .andExpect(jsonPath("$.path").value("/api/products/1"));
    }
}
