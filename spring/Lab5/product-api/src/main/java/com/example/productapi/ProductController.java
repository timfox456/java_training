package com.example.productapi;

import com.example.productapi.dto.ProductRequest;
import com.example.productapi.dto.ProductResponse;
import com.example.productapi.ProductService; // <-- CHANGED: No longer in 'service' package

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private ProductResponse convertToResponseDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    private Product convertToEntity(ProductRequest productRequest) {
        if (productRequest == null) {
            return null;
        }
        return new Product(productRequest.getName(), productRequest.getDescription(), productRequest.getPrice());
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(@PageableDefault(size = 5, sort = "name") Pageable pageable) {
        return productService.getAllProducts(pageable)
                             .map(this::convertToResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return new ResponseEntity<>(convertToResponseDto(product), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product productToSave = convertToEntity(productRequest);
        Product newProduct = productService.addProduct(productToSave);
        return new ResponseEntity<>(convertToResponseDto(newProduct), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        Product updatedEntityDetails = convertToEntity(productRequest);
        Product updatedProduct = productService.updateProduct(id, productRequest); // <-- CORRECTED LINE
        return new ResponseEntity<>(convertToResponseDto(updatedProduct), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
