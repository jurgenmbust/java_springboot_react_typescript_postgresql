package com.inventory.service;
import com.inventory.dto.*;
import com.inventory.entity.*;
import com.inventory.exception.*;
import com.inventory.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Tests business rules without starting Spring or a database. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    ProductServiceImpl service;
    @BeforeEach void setup() { service = new ProductServiceImpl(products, categories, 5); }
    private ProductRequest input() { return new ProductRequest(" Keyboard ", "", "key-01", new BigDecimal("19.99"), 3, 1L); }
    @Test void createsProductAndNormalizesFields() {
        when(categories.findById(1L)).thenReturn(Optional.of(new Category()));
        when(products.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        ProductResponse result = service.create(input());
        assertEquals("KEY-01", result.sku()); assertEquals("Keyboard", result.name());
        assertEquals(new BigDecimal("19.99"), result.price());
    }
    @Test void rejectsDuplicateSku() {
        when(products.existsBySku("KEY-01")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(input()));
        verify(products, never()).saveAndFlush(any());
    }
    @Test void rejectsMissingCategory() {
        assertThrows(NotFoundException.class, () -> service.create(input()));
    }
    @Test void rejectsMissingProduct() { assertThrows(NotFoundException.class, () -> service.get(9L)); }
    @Test void rejectsUnsafeSortAndPageSize() {
        assertThrows(IllegalArgumentException.class, () -> service.list(null, null, false, 0, 101, "name", "asc"));
        assertThrows(IllegalArgumentException.class, () -> service.list(null, null, false, 0, 10, "category.password", "asc"));
    }
    @Test void deletesAnExistingProduct() {
        Product product = new Product(); when(products.findById(1L)).thenReturn(Optional.of(product));
        service.delete(1L); verify(products).delete(product); verify(products).flush();
    }
}
