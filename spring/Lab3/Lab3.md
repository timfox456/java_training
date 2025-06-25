### Part 3: Unit Testing the Service Layer (Mockito)

Unit tests focus on individual components (like `ProductService`) in isolation, typically mocking their dependencies.

1. **Create a new test file:** In the `src/test/java/com/example/productapi` directory, create a new file named `ProductServiceTests.java`.
    
2. **Add `ProductServiceTests` class:** Copy and paste the following code into `ProductServiceTests.java`:
3. 
    ```java
    package com.example.productapi;
    
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    
    import java.util.Arrays;
    import java.util.List;
    import java.util.Optional;
    
    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;
    
    @ExtendWith(MockitoExtension.class) // Integrates Mockito with JUnit 5
    public class ProductServiceTests {
    
        @Mock // This will be a mock object for ProductRepository
        private ProductRepository productRepository;
    
        @InjectMocks // Inject the mocked repository into ProductService
        private ProductService productService;
    
        private Product product1;
        private Product product2;
    
        @BeforeEach // This method runs before each test
        void setUp() {
            // Initialize dummy products for testing
            product1 = new Product(1L, "Test Laptop", "Description 1", 1000.0);
            product2 = new Product(2L, "Test Mouse", "Description 2", 20.0);
        }
    
        @Test
        void testGetAllProducts() {
            // Define behavior for the mock repository
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
    
            List<Product> products = productService.getAllProducts();
    
            // Assertions
            assertNotNull(products);
            assertEquals(2, products.size());
            assertEquals("Test Laptop", products.get(0).getName());
            // Verify that findAll() was called on the mock
            verify(productRepository, times(1)).findAll();
        }
    
        @Test
        void testGetProductByIdFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    
            Optional<Product> foundProduct = productService.getProductById(1L);
    
            assertTrue(foundProduct.isPresent());
            assertEquals("Test Laptop", foundProduct.get().getName());
            verify(productRepository, times(1)).findById(1L);
        }
    
        @Test
        void testGetProductByIdNotFound() {
            when(productRepository.findById(3L)).thenReturn(Optional.empty());
    
            Optional<Product> foundProduct = productService.getProductById(3L);
    
            assertFalse(foundProduct.isPresent());
            verify(productRepository, times(1)).findById(3L);
        }
    
        @Test
        void testAddProduct() {
            Product newProduct = new Product(null, "New Product", "New Desc", 50.0);
            // Mock the save operation to return the product with an ID after saving
            when(productRepository.save(any(Product.class))).thenReturn(new Product(3L, "New Product", "New Desc", 50.0));
    
            Product createdProduct = productService.addProduct(newProduct);
    
            assertNotNull(createdProduct.getId());
            assertEquals("New Product", createdProduct.getName());
            verify(productRepository, times(1)).save(any(Product.class));
        }
    
        @Test
        void testUpdateProductFound() {
            Product existingProduct = new Product(1L, "Old Name", "Old Desc", 10.0);
            Product updatedDetails = new Product(null, "Updated Name", "Updated Desc", 20.0);
    
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(updatedDetails); // Mock save returns the updated product
    
            Optional<Product> result = productService.updateProduct(1L, updatedDetails);
    
            assertTrue(result.isPresent());
            assertEquals("Updated Name", result.get().getName());
            assertEquals(20.0, result.get().getPrice());
    
            // Verify findById was called, then save was called with the modified product
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(existingProduct); // Mockito will check if save was called with 'existingProduct' after its fields were changed
        }
    
    
        @Test
        void testUpdateProductNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());
    
            Optional<Product> result = productService.updateProduct(99L, new Product());
    
            assertFalse(result.isPresent());
            verify(productRepository, times(1)).findById(99L);
            verify(productRepository, never()).save(any(Product.class)); // save should not be called
        }
    
        @Test
        void testDeleteProductExisting() {
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L); // Configure void method mock
    
            boolean result = productService.deleteProduct(1L);
    
            assertTrue(result);
            verify(productRepository, times(1)).existsById(1L);
            verify(productRepository, times(1)).deleteById(1L);
        }
    
        @Test
        void testDeleteProductNonExisting() {
            when(productRepository.existsById(99L)).thenReturn(false);
    
            boolean result = productService.deleteProduct(99L);
    
            assertFalse(result);
            verify(productRepository, times(1)).existsById(99L);
            verify(productRepository, never()).deleteById(anyLong()); // deleteById should not be called
        }
    }
    ```
    
    _Self-correction tip:_ Ensure all JUnit 5 (`org.junit.jupiter.api`) and Mockito (`org.mockito`) imports are correct.
    
3. **Run Service Tests:**
    
    - Open the **Testing** view in VS Code (the beaker icon in the Activity Bar).
        
    - You should see `ProductServiceTests` listed.
        
    - Click the **play button** next to `ProductServiceTests` to run all tests in the class, or next to individual methods to run specific tests.
        
    - All tests should pass (green checkmarks).
        

---

### Part 4: Integration Testing the Controller Layer (`@WebMvcTest`)

Integration tests for the controller focus on the web layer, ensuring that your endpoints map correctly and handle requests/responses as expected. We'll mock the service layer to isolate the controller's logic.

1. **Create a new test file:** In the `src/test/java/com/example/productapi` directory, create a new file named `ProductControllerTests.java`.
    
2. **Add `ProductControllerTests` class:** Copy and paste the following code into `ProductControllerTests.java`:
    
    ```java
    package com.example.productapi;
    
    import com.fasterxml.jackson.databind.ObjectMapper; // NEW for JSON conversion
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.MediaType;
    import org.springframework.test.web.servlet.MockMvc;
    
    import java.util.Arrays;
    import java.util.Optional;
    
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
    
    @WebMvcTest(ProductController.class) // Focuses test on ProductController, loads minimal Spring context
    public class ProductControllerTests {
    
        @Autowired
        private MockMvc mockMvc; // Used to perform simulated HTTP requests
    
        @MockBean // Creates a Mockito mock and registers it as a Spring Bean
        private ProductService productService;
    
        @Autowired
        private ObjectMapper objectMapper; // Helper to convert objects to JSON and vice-versa
    
        private Product product1;
        private Product product2;
    
        @BeforeEach
        void setUp() {
            product1 = new Product(1L, "Test Laptop", "Desc L", 1000.0);
            product2 = new Product(2L, "Test Mouse", "Desc M", 20.0);
        }
    
        @Test
        void testGetAllProducts() throws Exception {
            when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));
    
            mockMvc.perform(get("/api/products"))
                   .andExpect(status().isOk()) // Expect 200 OK
                   .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Expect JSON content
                   .andExpect(jsonPath("$[0].name").value("Test Laptop")) // Check first product's name
                   .andExpect(jsonPath("$.length()").value(2)); // Check array size
    
            verify(productService, times(1)).getAllProducts();
        }
    
        @Test
        void testGetProductByIdFound() throws Exception {
            when(productService.getProductById(1L)).thenReturn(Optional.of(product1));
    
            mockMvc.perform(get("/api/products/1"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                   .andExpect(jsonPath("$.name").value("Test Laptop"));
    
            verify(productService, times(1)).getProductById(1L);
        }
    
        @Test
        void testGetProductByIdNotFound() throws Exception {
            when(productService.getProductById(99L)).thenReturn(Optional.empty());
    
            mockMvc.perform(get("/api/products/99"))
                   .andExpect(status().isNotFound()); // Expect 404 Not Found
    
            verify(productService, times(1)).getProductById(99L);
        }
    
        @Test
        void testAddProductValid() throws Exception {
            Product newProduct = new Product(null, "New Keyboard", "Gaming", 150.0);
            Product savedProduct = new Product(3L, "New Keyboard", "Gaming", 150.0);
    
            when(productService.addProduct(any(Product.class))).thenReturn(savedProduct);
    
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newProduct))) // Convert Product to JSON string
                   .andExpect(status().isCreated()) // Expect 201 Created
                   .andExpect(jsonPath("$.id").value(3L))
                   .andExpect(jsonPath("$.name").value("New Keyboard"));
    
            verify(productService, times(1)).addProduct(any(Product.class));
        }
    
        @Test
        void testAddProductInvalidName() throws Exception {
            Product invalidProduct = new Product(null, "a", "Invalid name short", 10.0); // Name too short
    
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProduct)))
                   .andExpect(status().isBadRequest()) // Expect 400 Bad Request due to validation
                   .andExpect(jsonPath("$.errors").exists()); // Spring's default error structure for validation
        }
    
        @Test
        void testAddProductInvalidPrice() throws Exception {
            Product invalidProduct = new Product(null, "Valid Name", "Negative Price", -5.0); // Price negative
    
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProduct)))
                   .andExpect(status().isBadRequest()); // Expect 400 Bad Request
        }
    
        @Test
        void testUpdateProductValid() throws Exception {
            Product updatedDetails = new Product(1L, "Updated Laptop", "Updated Desc", 1200.0);
            when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(Optional.of(updatedDetails));
    
            mockMvc.perform(put("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedDetails)))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.name").value("Updated Laptop"));
    
            verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
        }
    
        @Test
        void testUpdateProductInvalidName() throws Exception {
            Product invalidUpdate = new Product(1L, "", "Invalid name empty", 10.0); // Name empty
    
            mockMvc.perform(put("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdate)))
                   .andExpect(status().isBadRequest()); // Expect 400 Bad Request
        }
    
        @Test
        void testDeleteProductExisting() throws Exception {
            when(productService.deleteProduct(1L)).thenReturn(true);
    
            mockMvc.perform(delete("/api/products/1"))
                   .andExpect(status().isNoContent()); // Expect 204 No Content
    
            verify(productService, times(1)).deleteProduct(1L);
        }
    
        @Test
        void testDeleteProductNonExisting() throws Exception {
            when(productService.deleteProduct(99L)).thenReturn(false);
    
            mockMvc.perform(delete("/api/products/99"))
                   .andExpect(status().isNotFound()); // Expect 404 Not Found
    
            verify(productService, times(1)).deleteProduct(99L);
        }
    }
    ```
    
    _Self-correction tip:_ Ensure all necessary imports are present, especially those for `MockMvcRequestBuilders`, `MockMvcResultMatchers`, and `com.fasterxml.jackson.databind.ObjectMapper`. You might need to add `com.fasterxml.jackson.core:jackson-databind` if not already pulled in by `spring-boot-starter-web`. It usually is.
    
3. **Run Controller Tests:**
    
    - Open the **Testing** view in VS Code.
        
    - You should now see `ProductControllerTests` listed alongside `ProductServiceTests`.
        
    - Click the **play button** next to `ProductControllerTests` to run all tests in the class.
        
    - All tests should pass. Pay close attention to the validation tests, which expect a `400 Bad Request`.
        

---

### Part 5: Testing Your API (Manual with Thunder Client & `curl` for Validation)

Now, let's manually test the validation rules you just implemented. Start your Spring Boot application as usual.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button.
        
    - Ensure the application starts successfully on port 8080.
        

#### Testing with Thunder Client (VS Code)

Use Thunder Client to send requests.

1. **Create New Request:** Click "New Request".
    
    **a) POST (Create) a Valid Product (Success)**
    
    - **Method:** `POST`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste:
        
        JSON
        
        ```
        {
            "name": "Headphones",
            "description": "Noise-cancelling headphones.",
            "price": 200.00
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `201 Created` status and the JSON of the new product.
        
    
    **b) POST (Create) an Invalid Product (Empty Name)**
    
    - **Method:** `POST`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste (empty `name`):
        
        ```json
        {
            "name": "",
            "description": "Should fail due to empty name.",
            "price": 10.00
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `400 Bad Request` status. The response body will contain details about the validation errors, typically including the message "Product name is required".
        
    
    **c) POST (Create) an Invalid Product (Price too low)**
    
    - **Method:** `POST`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste (price `0.0`):
        
        ```json
        {
            "name": "Invalid Product",
            "description": "Should fail due to price <= 0.",
            "price": 0.0
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `400 Bad Request` status, with a validation error message like "Price must be greater than 0".
        
    
    **d) PUT (Update) an Invalid Product (Description too long)**
    
    - **Method:** `PUT`
        
    - **URL:** `http://localhost:8080/api/products/1` (Assuming ID 1 exists)
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste (long `description`):
        
    
        ```json
        {
            "name": "Laptop",
            "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. This description is definitely too long and should trigger a validation error.",
            "price": 1200.00
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `400 Bad Request` status, with a validation error message like "Description cannot exceed 500 characters".
        

---

#### `curl` Appendix for Lab 3 Testing
```bash
# Ensure your Spring Boot application is running before executing these.

echo "--- Lab 3: Validation Tests with curl ---"

# 1. POST (Create) a Valid Product (Success)
echo "--- POST Valid Product (Success) ---"
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "name": "Headphones", "description": "Noise-cancelling headphones.", "price": 200.00 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 2. POST (Create) an Invalid Product (Empty Name)
echo "--- POST Invalid Product (Empty Name) ---"
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "name": "", "description": "Should fail due to empty name.", "price": 10.00 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 3. POST (Create) an Invalid Product (Price too low)
echo "--- POST Invalid Product (Price too low) ---"
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "name": "Invalid Product", "description": "Should fail due to price <= 0.", "price": 0.0 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 4. PUT (Update) an Invalid Product (Description too long) - Assumes product with ID 1 exists
echo "--- PUT Invalid Product (Description too long) ---"
curl -X PUT \
     -H "Content-Type: application/json" \
     -d '{ "name": "Laptop", "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. This description is definitely too long and should trigger a validation error.", "price": 1200.00 }' \
     http://localhost:8080/api/products/1
echo ""
echo "----------------------------------------"
```

---

### Congratulations!

You have successfully implemented automated tests for both your service and controller layers, and added robust input validation to your API. This is crucial for building maintainable, reliable, and secure enterprise applications. You now have the tools to ensure your code works as expected and handles invalid data gracefully!
