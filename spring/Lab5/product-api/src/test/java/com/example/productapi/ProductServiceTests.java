package com.example.productapi;

import com.example.productapi.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal; // Import for BigDecimal
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository; // ProductRepository is now in com.example.productapi

    @InjectMocks
    private ProductService productService; // ProductService is now in com.example.productapi

    private Product product1;
    private Product product2;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        // Correct Product constructor usage: (name, description, price)
        product1 = new Product("Laptop Pro", "Powerful laptop for professionals", new BigDecimal("1200.00"));
        product1.setId(1L); // Set ID for simulating existing product

        product2 = new Product("Wireless Mouse", "Ergonomic wireless mouse", new BigDecimal("25.99"));
        product2.setId(2L); // Set ID for simulating existing product

        // ProductRequest DTO for update/create scenarios
        productRequest = new ProductRequest("Updated Laptop", "More powerful laptop", new BigDecimal("1300.50"));
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = Arrays.asList(product1, product2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(pageable);
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(product1.getName(), result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetProductByIdFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        Product foundProduct = productService.getProductById(1L);
        assertNotNull(foundProduct);
        assertEquals("Laptop Pro", foundProduct.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        // Assert that ProductNotFoundException is thrown
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(3L));
        verify(productRepository, times(1)).findById(3L);
    }

    @Test
    void testAddProduct() {
        Product newProduct = new Product("New Monitor", "4K HDR monitor", new BigDecimal("399.99"));
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product addedProduct = productService.addProduct(newProduct);
        assertNotNull(addedProduct);
        assertEquals("New Monitor", addedProduct.getName());
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void testUpdateProductFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1)); // Return original product
        when(productRepository.save(any(Product.class))).thenReturn(product1); // Save returns the updated product1

        // Use the ProductRequest DTO for updating
        Product updatedProduct = productService.updateProduct(1L, productRequest);

        assertNotNull(updatedProduct);
        assertEquals(1L, updatedProduct.getId());
        assertEquals("Updated Laptop", updatedProduct.getName());
        assertEquals(new BigDecimal("1300.50"), updatedProduct.getPrice()); // Ensure BigDecimal comparison
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product1); // Verify save was called
    }


    @Test
    void testUpdateProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Assert that ProductNotFoundException is thrown when updating non-existent product
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(99L, productRequest));
        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class)); // Save should not be called
    }

    @Test
    void testDeleteProductFound() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        boolean result = productService.deleteProduct(1L);
        assertTrue(result);
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        boolean result = productService.deleteProduct(99L);
        assertFalse(result);
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong()); // Should not attempt to delete
    }
}
