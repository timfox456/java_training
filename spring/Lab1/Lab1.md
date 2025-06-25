
## Lab 1: Building a Simple REST API (CRUD Operations)

**Goal:** In this lab, you will create your first Spring Boot application that exposes a simple RESTful API. This API will allow you to perform basic Create, Read, Update, and Delete (CRUD) operations on a resource called "Product" using an in-memory data store (a simple Java List).

**Concepts You'll Learn:**

- Setting up a Spring Boot project.
    
- Creating REST controllers and handling HTTP requests.
    
- Defining data models (POJOs).
    
- Managing data in memory.
    
- Testing your API using tools like Thunder Client (VS Code extension) or curl.
    

---

### Part 1: Project Setup with Spring Initializr

Spring Initializr is the fastest way to get a new Spring Boot project up and running.

1. **Open your Web Browser:** Go to [https://start.spring.io/](https://start.spring.io/)
    
2. **Configure Your Project:** Fill out the form as follows:
    
    - **Project:** `Maven Project` (or `Gradle Project` if you prefer Gradle, but these instructions will assume Maven).
        
    - **Language:** `Java`
        
    - **Spring Boot:** Choose the latest stable version (e.g., `3.3.1` or similar, **avoiding SNAPSHOT/M versions**).
        
    - **Group:** `com.example` (or your company's domain, e.g., `com.yourcompany.products`)
        
    - **Artifact:** `product-api` (This will be the name of your project folder and main JAR file)
        
    - **Name:** `product-api` (defaults to Artifact name)
        
    - **Description:** `Demo project for a simple Product API`
        
    - **Package Name:** `com.example.productapi` (defaults to Group + Artifact)
        
    - **Packaging:** `Jar`
        
    - **Java:** Choose the Java version you are using (e.g., `17` or `21`).
        
3. **Add Dependencies:** In the "Dependencies" section, click "Add Dependencies..." and search for and select:
    
    - `Spring Web` (This is crucial for building REST APIs)
        
4. **Generate and Download:** Click the "GENERATE" button at the bottom. This will download a `.zip` file to your computer (e.g., `product-api.zip`).
    
5. **Extract the Project:**
    
    - Navigate to your Downloads folder.
        
    - Unzip `product-api.zip` into a location where you want to keep your projects (e.g., a `java_labs` folder). You should now have a folder named `product-api` containing your project files.
        

---

### Part 2: Open and Run in VS Code

Now, let's open the project in VS Code and make sure it runs correctly.

1. **Open VS Code.**
    
2. **Open Folder:** Go to `File > Open Folder...` (or `Cmd+K Cmd+O` on Mac, `Ctrl+K Ctrl+O` on Windows/Linux).
    
3. **Navigate and Select:** Browse to the `product-api` folder you just unzipped and click "Select Folder".
    
4. **Install Extensions (if prompted):** VS Code will likely detect the Java project and recommend installing the "Extension Pack for Java" (if you don't already have it). Click "Install" if prompted. This pack includes essential tools like Language Support for Java, Debugger for Java, Maven for Java, etc.
    
5. **Allow Workspace Trust:** If VS Code asks "Do you trust the authors of the files in this folder?", click "Yes, I trust the authors".
    
6. **Wait for Project Initialization:** In the bottom right corner, you'll see activity as VS Code imports the Maven project, downloads dependencies, and builds the workspace. Wait for this to complete. It might take a few minutes for the first time.
    
7. **Run the Application:**
    
    - Open `src/main/java/com/example/productapi/ProductApiApplication.java` (your main application file).
        
    - You should see a green "Run" button and a "Debug" button above the `main` method. Click the **"Run"**button.
        
    - Alternatively, open the **Run and Debug** view (Ctrl+Shift+D) and click the "Run and Debug" button, then select "Java".
        
    - You'll see output in the "DEBUG CONSOLE" or "TERMINAL" panel. Look for a message similar to:
        
        ```console
        Started ProductApiApplication in ... seconds (JVM running for ...)
        ```
        
        And importantly:
        
        ```console
        Tomcat started on port(s): 8080 (http)
        ```
        
    - This means your Spring Boot application is running and listening on port 8080!
        
8. **Stop the Application:** Click the **red square "Stop" button** in the "DEBUG CONSOLE" toolbar, or press `Ctrl+C` in the terminal where it's running.
    

---

### Part 3: Create the Product Model

First, we need a simple Java class to represent our "Product" resource.

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `Product.java`.
    
2. **Add the Product class:** Copy and paste the following code into `Product.java`:
    
    
    ```java
    package com.example.productapi;
    
    public class Product {
        private Long id;
        private String name;
        private String description;
        private double price;
    
        // Constructor
        public Product(Long id, String name, String description, double price) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
        }
    
        // Default constructor (required by some frameworks, good practice)
        public Product() {
        }
    
        // Getters and Setters
        public Long getId() {
            return id;
        }
    
        public void setId(Long id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public String getDescription() {
            return description;
        }
    
        public void setDescription(String description) {
            this.description = description;
        }
    
        public double getPrice() {
            return price;
        }
    
        public void setPrice(double price) {
            this.price = price;
        }
    
        @Override
        public String toString() {
            return "Product{" +
                   "id=" + id +
                   ", name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", price=" + price +
                   '}';
        }
    }
    ```
    
    _Self-correction tip:_ If you get red squiggly lines, ensure you've saved the file (`Ctrl+S` / `Cmd+S`) and that the `package` declaration matches your actual project package.
    

---

### Part 4: Create an In-Memory Data Store (ProductService)

We'll create a simple "Service" class to manage our list of products in memory.

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `ProductService.java`.
    
2. **Add the ProductService class:** Copy and paste the following code into `ProductService.java`:
    
    
    ```java
    package com.example.productapi;
    
    import org.springframework.stereotype.Service;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.concurrent.atomic.AtomicLong;
    
    @Service // Marks this class as a Spring Service component
    public class ProductService {
    
        private final List<Product> products = new ArrayList<>();
        private final AtomicLong idCounter = new AtomicLong(); // For generating unique IDs
    
        // Initialize with some dummy data
        public ProductService() {
            products.add(new Product(idCounter.incrementAndGet(), "Laptop", "Powerful laptop for coding.", 1200.00));
            products.add(new Product(idCounter.incrementAndGet(), "Mouse", "Ergonomic wireless mouse.", 25.00));
            products.add(new Product(idCounter.incrementAndGet(), "Keyboard", "Mechanical keyboard with RGB.", 150.00));
        }
    
        public List<Product> getAllProducts() {
            return products;
        }
    
        public Optional<Product> getProductById(Long id) {
            return products.stream()
                           .filter(product -> product.getId().equals(id))
                           .findFirst();
        }
    
        public Product addProduct(Product product) {
            product.setId(idCounter.incrementAndGet()); // Assign a new ID
            products.add(product);
            return product;
        }
    
        public Optional<Product> updateProduct(Long id, Product updatedProduct) {
            return products.stream()
                           .filter(product -> product.getId().equals(id))
                           .findFirst()
                           .map(product -> {
                               product.setName(updatedProduct.getName());
                               product.setDescription(updatedProduct.getDescription());
                               product.setPrice(updatedProduct.getPrice());
                               return product;
                           });
        }
    
        public boolean deleteProduct(Long id) {
            return products.removeIf(product -> product.getId().equals(id));
        }
    }
    ```
    
    _Self-correction tip:_ Make sure you import all necessary classes (`ArrayList`, `List`, `Optional`, `AtomicLong`, `Service`). Use `Ctrl+.` (or `Cmd+.`) to quick-fix imports.
    

---

### Part 5: Create the REST Controller

This is where we'll define the API endpoints that clients (like web browsers or other applications) can call.

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `ProductController.java`.
    
2. **Add the ProductController class:** Copy and paste the following code into `ProductController.java`:
    
    
    ```java
    package com.example.productapi;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    
    import java.util.List;
    
    @RestController // Marks this class as a REST Controller
    @RequestMapping("/api/products") // Base path for all endpoints in this controller
    public class ProductController {
    
        private final ProductService productService;
    
        // Spring automatically injects ProductService here (Dependency Injection)
        @Autowired
        public ProductController(ProductService productService) {
            this.productService = productService;
        }
    
        // GET all products
        @GetMapping
        public List<Product> getAllProducts() {
            return productService.getAllProducts();
        }
    
        // GET a product by ID
        @GetMapping("/{id}")
        public ResponseEntity<Product> getProductById(@PathVariable Long id) {
            return productService.getProductById(id)
                    .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
    
        // POST (Create) a new product
        @PostMapping
        public ResponseEntity<Product> addProduct(@RequestBody Product product) {
            Product newProduct = productService.addProduct(product);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED); // Respond with 201 Created
        }
    
        // PUT (Update) an existing product
        @PutMapping("/{id}")
        public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
            return productService.updateProduct(id, product)
                    .map(updatedProduct -> new ResponseEntity<>(updatedProduct, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
    
        // DELETE a product
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
            if (productService.deleteProduct(id)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Respond with 204 No Content
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Product not found
            }
        }
    }
    ```
    
    _Self-correction tip:_ Again, ensure all necessary imports are present (`Autowired`, `HttpStatus`, `ResponseEntity`, `RestController`, `RequestMapping`, `GetMapping`, `PostMapping`, `PutMapping`, `DeleteMapping`, `PathVariable`, `RequestBody`).
    

---

### Part 6: Testing Your API with Thunder Client (VS Code)

Thunder Client is a lightweight REST API client built directly into VS Code, making it very convenient for testing.

1. **Install Thunder Client Extension:**
    
    - If you don't have it, open the Extensions view in VS Code (Ctrl+Shift+X or Cmd+Shift+X).
        
    - Search for "Thunder Client" and click "Install".
        
2. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button above the `main` method (or use the Run and Debug view).
        
    - Ensure the application starts successfully on port 8080.
        
3. **Open Thunder Client:** Click on the Thunder Client icon in the VS Code Activity Bar (usually on the left side, looks like a lightning bolt).
    
4. **Create New Request:** Click "New Request".
    

#### Test 1: GET All Products

- **Method:** `GET`
    
- **URL:** `http://localhost:8080/api/products`
    
- **Send:** Click the "Send" button.
    
- **Expected Response:** You should see a `200 OK` status and a JSON array containing the initial three products:
    
    
    ```json
    [
        {
            "id": 1,
            "name": "Laptop",
            "description": "Powerful laptop for coding.",
            "price": 1200.0
        },
        {
            "id": 2,
            "name": "Mouse",
            "description": "Ergonomic wireless mouse.",
            "price": 25.0
        },
        {
            "id": 3,
            "name": "Keyboard",
            "description": "Mechanical keyboard with RGB.",
            "price": 150.0
        }
    ]
    ```
    

#### Test 2: GET Product by ID

- **Method:** `GET`
    
- **URL:** `http://localhost:8080/api/products/1` (Try with `2`, `3` as well)
    
- **Send:** Click the "Send" button.
    
- **Expected Response:** `200 OK` and the JSON for the product with ID 1 (the Laptop).
    
- **Test Not Found:** Change the URL to `http://localhost:8080/api/products/99` (an ID that doesn't exist). You should get a `404 Not Found` status.
    

#### Test 3: POST (Create) a New Product

- **Method:** `POST`
    
- **URL:** `http://localhost:8080/api/products`
    
- **Headers:**
    
    - `Content-Type`: `application/json`
        
- **Body:** Select `JSON` and paste the following:
    
    
    ```json
    {
        "name": "Monitor",
        "description": "27-inch 4K monitor.",
        "price": 450.00
    }
    ```
    
- **Send:** Click the "Send" button.
    
- **Expected Response:** `201 Created` status and the JSON of the new product, including its newly assigned ID (e.g., ID 4).
    
    _Now, try the `GET /api/products` endpoint again to see if the new product is in the list!_
    

#### Test 4: PUT (Update) an Existing Product

- **Method:** `PUT`
    
- **URL:** `http://localhost:8080/api/products/1` (Update the Laptop)
    
- **Headers:**
    
    - `Content-Type`: `application/json`
        
- **Body:** Select `JSON` and paste the following (change the price):
    
    
    ```json
    {
        "name": "Laptop",
        "description": "Powerful laptop for coding with new CPU.",
        "price": 1300.00
    }
    ```
    
- **Send:** Click the "Send" button.
    
- **Expected Response:** `200 OK` status and the JSON of the updated product.
    
    _Verify with `GET /api/products/1`._
    

#### Test 5: DELETE a Product

- **Method:** `DELETE`
    
- **URL:** `http://localhost:8080/api/products/2` (Delete the Mouse)
    
- **Send:** Click the "Send" button.
    
- **Expected Response:** `204 No Content` status.
    
    _Verify with `GET /api/products`. The Mouse should be gone._ _Try deleting ID 2 again. You should get `404 Not Found`._
    

---

### Congratulations!

You have successfully built and tested your first Spring Boot REST API with basic CRUD operations! This is a fundamental building block for many modern applications.

**What's next?** In the next labs, you'll learn how to replace the in-memory `List` with a real database and how to write automated tests for your API.


## Appendix: `curl` Commands for Lab 1 Testing

If you prefer using the command line or encounter issues with a GUI client, `curl` is an excellent tool for testing REST APIs. Ensure your Spring Boot application is running on `http://localhost:8080` before executing these commands.

---

### Test 1: GET All Products

This command retrieves all products currently stored in the in-memory list.


```bash
curl -X GET http://localhost:8080/api/products
```

**Expected Output:** A JSON array of products, similar to:


```json
[
    {"id":1,"name":"Laptop","description":"Powerful laptop for coding.","price":1200.0},
    {"id":2,"name":"Mouse","description":"Ergonomic wireless mouse.","price":25.0},
    {"id":3,"name":"Keyboard","description":"Mechanical keyboard with RGB.","price":150.0}
]
```

---

### Test 2: GET Product by ID

This command retrieves a single product by its ID. Replace `1` with `2`, `3`, or a non-existent ID to test different scenarios.

```bash
curl -X GET http://localhost:8080/api/products/1
```

**Expected Output (for ID 1):**


```json
{"id":1,"name":"Laptop","description":"Powerful laptop for coding.","price":1200.0}
```

**Expected Output (for non-existent ID, e.g., `/api/products/99`):** No content, and a `404 Not Found` HTTP status.

---

### Test 3: POST (Create) a New Product

This command sends a new product's data to the API to create it.


```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "name": "Monitor", "description": "27-inch 4K monitor.", "price": 450.00 }' \
     http://localhost:8080/api/products
```

**Expected Output:** A `201 Created` HTTP status and the JSON representation of the newly created product, including its new ID (e.g., `4`).


```json
{"id":4,"name":"Monitor","description":"27-inch 4K monitor.","price":450.0}
```

_After running this, re-run Test 1 (`GET /api/products`) to confirm the new product is in the list._

---

### Test 4: PUT (Update) an Existing Product

This command updates an existing product. Replace `1` with the ID of the product you wish to modify.

```bash
curl -X PUT \
     -H "Content-Type: application/json" \
     -d '{ "name": "Laptop Pro", "description": "Powerful laptop for coding with new CPU.", "price": 1350.00 }' \
     http://localhost:8080/api/products/1
```

**Expected Output:** A `200 OK` HTTP status and the JSON representation of the updated product.

```bash
{"id":1,"name":"Laptop Pro","description":"Powerful laptop for coding with new CPU.","price":1350.0}
```

_After running this, re-run Test 1 or `GET /api/products/1` to confirm the updates._

---

### Test 5: DELETE a Product

This command deletes a product by its ID. Replace `2` with the ID of the product you wish to delete (e.g., the "Mouse").

```bash
curl -X DELETE http://localhost:8080/api/products/2
```

**Expected Output:** A `204 No Content` HTTP status (indicating success with no body returned).

_After running this, re-run Test 1 (`GET /api/products`) to confirm the product is removed from the list. If you try to delete the same ID again, you should receive a `404 Not Found` status.


