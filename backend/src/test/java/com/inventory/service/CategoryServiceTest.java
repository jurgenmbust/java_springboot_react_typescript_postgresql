package com.inventory.service;
import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.exception.*;
import com.inventory.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Checks category rules separately from HTTP and PostgreSQL. */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock CategoryRepository categories;
    @Mock ProductRepository products;
    @InjectMocks CategoryServiceImpl service;
    @Test void createsAndTrimsCategory() {
        when(categories.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals("Office", service.create(new CategoryRequest(" Office ", null)).name());
    }
    @Test void rejectsDuplicateName() {
        when(categories.existsByNameIgnoreCase("Office")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(new CategoryRequest("Office", "")));
    }
    @Test void preventsDeletingUsedCategory() {
        when(categories.findById(1L)).thenReturn(Optional.of(new Category()));
        when(products.existsByCategoryId(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L));
        verify(categories, never()).delete(any());
    }
    @Test void deletesEmptyCategory() {
        Category category = new Category(); when(categories.findById(1L)).thenReturn(Optional.of(category));
        service.delete(1L); verify(categories).delete(category);
    }
    @Test void rejectsMissingCategory() { assertThrows(NotFoundException.class, () -> service.get(99L)); }
}
