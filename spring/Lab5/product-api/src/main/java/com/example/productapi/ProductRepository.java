package com.example.productapi; // <-- CHANGED: Now directly in com.example.productapi

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA automatically provides CRUD methods.
}
