## Lab 10: Asynchronous Processing with `@Async`

**Goal:** In this lab, you will learn how to use Spring Boot's `@Async` annotation to run methods in a separate thread. This is particularly useful for operations that might take a significant amount of time (e.g., sending emails, processing large files, external API calls) so that your main application thread (e.g., handling an HTTP request) can return a response to the client more quickly.

**Concepts You'll Learn:**

- **`@EnableAsync`:** The annotation to activate Spring's asynchronous method execution capability.
    
- **`@Async`:** The annotation used to mark a method for asynchronous execution.
    
- **Thread Pools:** Understanding that `@Async` methods run on a separate thread pool (Spring's default `TaskExecutor`or a custom one).
    
- **Non-Blocking Operations:** How `@Async` allows the calling thread to continue immediately without waiting for the asynchronous method to complete.
    
- **Observing Asynchronous Behavior:** How to verify that tasks are indeed running on different threads and not blocking the main flow.
    

**Prerequisites:**

- Completed Lab 9 successfully.
    
- Your `product-api` project from Lab 9.
    

---

### Part 1: Enable Asynchronous Processing

First, you need to enable the `@Async` functionality in your Spring Boot application.

1. **Open `ProductApiApplication.java`:** In your `product-api` project, navigate to `src/main/java/com/example/productapi/ProductApiApplication.java`.
    
2. **Add `@EnableAsync` Annotation:** Add the `@EnableAsync` annotation directly above your `ProductApiApplication`class, alongside `@SpringBootApplication` and `@EnableScheduling`.
    
    ```java
    package com.example.productapi;
    
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.scheduling.annotation.EnableScheduling;
    import org.springframework.scheduling.annotation.EnableAsync; // NEW
    
    @SpringBootApplication
    @EnableScheduling
    @EnableAsync // NEW: Enables asynchronous method execution
    public class ProductApiApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(ProductApiApplication.class, args);
        }
    
    }
    ```
    
    _Self-correction tip:_ Ensure `import org.springframework.scheduling.annotation.EnableAsync;` is added.
    
3. **Save `ProductApiApplication.java`:** Save the file (`Ctrl+S` / `Cmd+S`).
    

---

### Part 2: Create an Asynchronous Service Method

Now, let's create a new method within your `ProductService` that will simulate a long-running operation and be executed asynchronously.

1. **Open `ProductService.java`:** Navigate to `src/main/java/com/example/productapi/ProductService.java`.
    
2. **Add an Asynchronous Method:** Add the following method to your `ProductService` class. We'll use `Thread.sleep()` to simulate a delay, and `Logger` to show when the async task starts and finishes, and on which thread.
    
    ```java
    package com.example.productapi;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.scheduling.annotation.Async; // NEW
    import org.slf4j.Logger; // NEW
    import org.slf4j.LoggerFactory; // NEW
    
    import java.util.List;
    import java.util.Optional;
    
    @Service
    public class ProductService {
    
        private static final Logger logger = LoggerFactory.getLogger(ProductService.class); // NEW
    
        private final ProductRepository productRepository;
    
        @Autowired
        public ProductService(ProductRepository productRepository) {
            this.productRepository = productRepository;
        }
    
        // ... (existing methods like getAllProducts, getProductById, addProduct, updateProduct, deleteProduct) ...
    
        // NEW: Asynchronous method
        @Async // This method will be executed in a separate thread
        public void processAfterProductCreation(Long productId, String productName) {
            logger.info("Async Task Started: Processing product ID {} ({}) in thread {}",
                        productId, productName, Thread.currentThread().getName());
            try {
                // Simulate a long-running operation, e.g., sending an email,
                // calling another microservice, generating a report, etc.
                Thread.sleep(5000); // Sleep for 5 seconds
            } catch (InterruptedException e) {
                logger.error("Async task interrupted for product ID {}: {}", productId, e.getMessage());
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
            logger.info("Async Task Finished: Processing product ID {} ({}) completed.", productId, productName);
        }
    }
    ```
    
    _Self-correction tip:_ Ensure `import org.springframework.scheduling.annotation.Async;`, `import org.slf4j.Logger;`, and `import org.slf4j.LoggerFactory;` are added.
    
3. **Save `ProductService.java`:** Save the file.
    

---

### Part 3: Call the Asynchronous Method from the Controller

Now, we'll modify the `addProduct` method in your `ProductController` to call this new asynchronous method. This will demonstrate how the API request can complete quickly while the background processing continues.

1. **Open `ProductController.java`:** Navigate to `src/main/java/com/example/productapi/ProductController.java`.
    
2. **Call the Asynchronous Method in `addProduct`:** Modify the `addProduct` method to call `productService.processAfterProductCreation()` _after_ the product has been saved.
    
    
    ```java
    package com.example.productapi;
    
    import com.example.productapi.dto.ProductRequest;
    import com.example.productapi.dto.ProductResponse;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import jakarta.validation.Valid;
    
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.web.PageableDefault;
    import org.slf4j.Logger; // NEW
    import org.slf4j.LoggerFactory; // NEW
    
    import java.util.List;
    import java.util.stream.Collectors;
    
    @RestController
    @RequestMapping("/api/products")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public class ProductController {
    
        private static final Logger logger = LoggerFactory.getLogger(ProductController.class); // NEW
    
        private final ProductService productService;
    
        @Autowired
        public ProductController(ProductService productService) {
            this.productService = productService;
        }
    
        // ... (existing helper methods like convertToResponseDto, convertToEntity) ...
    
        // ... (existing GET, GET by ID, PUT, DELETE methods) ...
    
        // MODIFIED: Call asynchronous method after product creation
        @PostMapping
        public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest productRequest) {
            logger.info("Controller: Received request to add product: {}", productRequest.getName()); // NEW
            Product productToSave = convertToEntity(productRequest);
            Product newProduct = productService.addProduct(productToSave);
    
            // Trigger the asynchronous background process
            productService.processAfterProductCreation(newProduct.getId(), newProduct.getName()); // NEW
    
            logger.info("Controller: Responding for product {} (Async task initiated).", newProduct.getName()); // NEW
            return new ResponseEntity<>(convertToResponseDto(newProduct), HttpStatus.CREATED);
        }
    }
    ```
    
    _Self-correction tip:_ Ensure `import org.slf4j.Logger;` and `import org.slf4j.LoggerFactory;` are added to `ProductController`.
    
3. **Save `ProductController.java`:** Save the file.
    

---

### Part 4: Observe Asynchronous Behavior

Now, run your application and make a `POST` request to create a new product. You will observe that the API call returns quickly, while the log messages from the asynchronous method appear later, indicating it's running in the background.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button.
        
    - Ensure the application starts successfully on port 8080.
        
    - **Pay close attention to the VS Code "DEBUG CONSOLE" or "TERMINAL" output.**
        
2. **Make a `POST` request to add a new product:** Use Thunder Client (or `curl` from your terminal) to send a `POST`request. Remember to use `admin:adminpass` (or `user:password` if you've allowed it to POST).
    
    - **Thunder Client:**
        
        - **Method:** `POST`
            
        - **URL:** `http://localhost:8080/api/products`
            
        - **Auth:** `Basic Auth` (`admin:adminpass`)
            
        - **Headers:** `Content-Type: application/json`
            
        - **Body:** Select `JSON` and paste:
            
            JSON
            
            ```json
            {
                "name": "Smart Watch",
                "description": "Wearable tech with health tracking.",
                "price": 199.99
            }
            ```
            
        - **Send.**
            
    - **`curl` (Windows Command Prompt/PowerShell):**
        
        ```powershell
        curl -v -X POST `
             -H "Content-Type: application/json" `
             -u admin:adminpass `
             -d '{ "name": "Smart Watch", "description": "Wearable tech with health tracking.", "price": 199.99 }' `
             http://localhost:8080/api/products
        ```
        
    - **`curl` (macOS/Linux / Git Bash on Windows):**
        
        Bash
        
        ```bash
        curl -v -X POST \
             -H "Content-Type: application/json" \
             -u admin:adminpass \
             -d '{ "name": "Smart Watch", "description": "Wearable tech with health tracking.", "price": 199.99 }' \
             http://localhost:8080/api/products
        ```
        
3. **Observe the Output:**
    
    - **HTTP Response:** The HTTP `POST` request should return a `201 Created` response **almost immediately**(within a few milliseconds). This indicates that the main thread handling the API request did _not_ wait for the `processAfterProductCreation` method to finish.
        
    - **Console Logs:** Look at your application's console/terminal output.
        
        - You'll see the `Controller: Received request...` and `Controller: Responding...` messages appear quickly.
            
        - **After about 5 seconds (the `Thread.sleep()` duration),** you will then see the `Async Task Started...` log, followed shortly by `Async Task Finished...`.
            
        - **Crucially, check the thread names in the logs!** The controller logs will typically show `http-nio-8080-exec-X` (the web server thread), while the asynchronous task logs will show `task-X` (from Spring's default asynchronous task executor). This confirms the task ran on a different thread.
            
    
    **Example Console Output Snippet:**
    
```console
    ... (application startup logs) ...
    
    # These appear almost instantly after you send the POST request:
    YYYY-MM-DDTHH:MM:SS.123 INFO  c.e.p.ProductController : Controller: Received request to add product: Smart Watch
    YYYY-MM-DDTHH:MM:SS.150 INFO  c.e.p.ProductController : Controller: Responding for product Smart Watch (Async task initiated).
    
    # ... (there might be other logs here, but these are delayed) ...
    
    # These appear approximately 5 seconds *after* the controller responded:
    YYYY-MM:DDTHH:MM:SS.151 INFO  c.e.p.ProductService    : Async Task Started: Processing product ID 4 (Smart Watch) in thread task-1
    YYYY-MM:DDTHH:MM:SS.152 DEBUG org.hibernate.SQL      : select product0_.id as id1_0_, product0_.description as descript2_0_, product0_.name as name3_0_, product0_.price as price4_0_ from product product0_ where product0_.id=?
    YYYY-MM:DDTHH:MM:SS.153 TRACE org.hibernate.type.descriptor.sql.BasicExtractor : extracted value ([id1_0_] : [BIGINT]) - [4]
    YYYY-MM:DDTHH:MM:SS.154 INFO  c.e.p.ProductService    : Async Task Finished: Processing product ID 4 (Smart Watch) completed.
    
    ... (other scheduled logs from Lab 8 might interleave here) ...
```
    
    _The exact timestamps and thread IDs (`task-1`, `http-nio-8080-exec-X`) will vary, but the order of appearance of the log messages is key._
    

---

#### `curl` Appendix for Lab 10 Testing



```bash
# Ensure your Spring Boot application is running before executing these.
# Pay close attention to the timestamps in your application's console output.

echo "--- Lab 10: Asynchronous Processing with @Async ---"

# POST a new product to trigger the asynchronous method
# This curl command should return an HTTP 201 CREATED response almost immediately.
# The 'Async Task Started/Finished' logs will appear in the application console later.

echo "--- POST /api/products (triggers @Async) ---"
curl -v -X POST \
     -H "Content-Type: application/json" \
     -u admin:adminpass \
     -d '{ "name": "Wireless Earbuds", "description": "High-fidelity audio, noise-cancelling.", "price": 129.99 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

echo "Observe your Spring Boot application's console."
echo "You should see the HTTP response come back quickly, but the 'Async Task' logs"
echo "will appear about 5 seconds later, confirming background execution."
```

---

### Congratulations!

You've successfully implemented and observed asynchronous method execution in Spring Boot using `@Async`! This technique is a cornerstone for building responsive and scalable applications by ensuring that long-running operations don't block critical request processing threads. You now have a powerful tool to improve the user experience and overall performance of your Spring Boot services.
