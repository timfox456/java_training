package com.example.productapi; // <-- CHANGED: Now directly in com.example.productapi

import com.example.productapi.Product;
import com.example.productapi.ProductRepository; // <-- CHANGED: No longer in 'repository' package
import com.example.productapi.ProductNotFoundException;
import com.example.productapi.dto.ProductRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id)
                                    .orElseThrow(() -> new ProductNotFoundException(id));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());

        return productRepository.save(existingProduct);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
