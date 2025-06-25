## Lab 2: Integrating with a Database (Spring Data JPA & H2)

**Goal:** In this lab, you will enhance your Spring Boot REST API from Lab 1 to persist data in an H2 in-memory database using Spring Data JPA. This moves you from a volatile in-memory list to a more realistic data persistence layer.

**Concepts You'll Learn:**

- Adding new Spring Boot dependencies (`spring-boot-starter-data-jpa`, `h2`).
    
- Configuring an embedded database in `application.properties`.
    
- Transforming a plain Java object into a JPA Entity (`@Entity`, `@Id`, `@GeneratedValue`).
    
- Creating a Spring Data JPA Repository (`JpaRepository`).
    
- Using the H2 Console to inspect database contents.
    
- Persisting and retrieving data using the repository.
    

**Prerequisites:**

- Completed Lab 1 successfully.
    
- Your `product-api` project from Lab 1.
    

---

### Part 1: Project Setup - Adding New Dependencies

First, we need to tell our Spring Boot project that we want to use Spring Data JPA and the H2 database.

1. **Open `pom.xml`:** In your `product-api` project in VS Code, open the `pom.xml` file located in the root directory.
    
2. **Add Dependencies:** Locate the `<dependencies>` section and add the following two new dependencies **inside** it, just like the `spring-boot-starter-web` dependency you already have:
    
    
    ```xml
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-data-jpa</artifactId>
    		</dependency>
    		<dependency>
    			<groupId>com.h2database</groupId>
    			<artifactId>h2</artifactId>
    			<scope>runtime</scope>
    		</dependency>
    ```
    
    Your `pom.xml` dependencies section should now look something like this (exact versions may vary):
    
    XML
    
    ```xml
    	<dependencies>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-web</artifactId>
    		</dependency>
    
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-data-jpa</artifactId>
    		</dependency>
    		<dependency>
    			<groupId>com.h2database</groupId>
    			<artifactId>h2</artifactId>
    			<scope>runtime</scope>
    		</dependency>
    
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-test</artifactId>
    			<scope>test</scope>
    		</dependency>
    	</dependencies>
    ```
    
3. **Save `pom.xml`:** Save the file (`Ctrl+S` / `Cmd+S`). VS Code's Java extension will automatically detect the changes and re-import the Maven project, downloading the new dependencies. You'll see activity in the bottom status bar. Wait for this process to complete.
    

---

### Part 2: Configure H2 Database

Now we'll configure Spring Boot to use the H2 database and enable its web console for easy inspection.

1. **Open `application.properties`:** In the `src/main/resources` folder, open the `application.properties` file. This file is used for Spring Boot configurations.
    
2. **Add H2 Configuration:** Add the following lines to `application.properties`. Each line configures a specific aspect of the H2 database and its console.
    
    
    ```properties
    # H2 Database Configuration
    spring.h2.console.enabled=true
    spring.h2.console.path=/h2-console
    spring.datasource.url=jdbc:h2:mem:productdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=password
    
    # JPA (Hibernate) Configuration
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    ```
    
    **Explanation:**
    
    - `spring.h2.console.enabled=true`: Enables the H2 web console.
        
    - `spring.h2.console.path=/h2-console`: Sets the URL path to access the console.
        
    - `spring.datasource.url=jdbc:h2:mem:productdb`: Specifies an in-memory H2 database named `productdb`. Data will be lost when the application stops.
        
    - `spring.datasource.username=sa`, `spring.datasource.password=password`: Default H2 login credentials.
        
    - `spring.jpa.hibernate.ddl-auto=update`: Tells Hibernate (JPA provider) to automatically create/update tables based on your JPA Entities.
        
    - `spring.jpa.show-sql=true`: Logs SQL statements executed by JPA to the console, useful for debugging.
        
3. **Save `application.properties`:** Save the file.
    

---

### Part 3: Transform Product Model to JPA Entity

Now, we need to tell JPA that our `Product` class represents a table in the database.

1. **Open `Product.java`:** Go back to your `Product.java` file (`src/main/java/com/example/productapi/Product.java`).
    
2. **Add JPA Annotations:** Modify the `Product` class by adding the following annotations:
    
    
    ```java
    package com.example.productapi;
    
    import jakarta.persistence.Entity; // Import from jakarta.persistence for Spring Boot 3+
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    
    @Entity // Marks this class as a JPA Entity (a table in the database)
    public class Product {
        @Id // Marks this field as the primary key
        @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate ID using database identity column
        private Long id;
        private String name;
        private String description;
        private double price;
    
        // Constructor (keep existing one)
        public Product(Long id, String name, String description, double price) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
        }
    
        // Default constructor (REQUIRED by JPA/Hibernate)
        public Product() {
        }
    
        // Getters and Setters (keep existing ones)
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
    
    **Important:** Notice the imports are now from `jakarta.persistence` for Spring Boot 3+. If you are on an older Spring Boot version (2.x), these would be `javax.persistence`. VS Code's quick-fix (`Ctrl+.` / `Cmd+.`) should help you import correctly.
    
3. **Save `Product.java`:** Save the file.
    

---

### Part 4: Create a Spring Data JPA Repository

Spring Data JPA makes database interaction incredibly easy. You just define an interface, and Spring generates the implementation at runtime!

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `ProductRepository.java`.
    
2. **Add the ProductRepository interface:** Copy and paste the following code into `ProductRepository.java`:
    
    
    ```java
    package com.example.productapi;
    
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    
    @Repository // Marks this as a Spring Data JPA repository
    public interface ProductRepository extends JpaRepository<Product, Long> {
        // Spring Data JPA automatically provides methods like:
        // findAll(), findById(id), save(entity), deleteById(id), count(), etc.
        // You don't need to write any implementation here!
    }
    ```
    
    _Self-correction tip:_ Ensure `JpaRepository` and `Repository` are imported from `org.springframework.data.jpa.repository` and `org.springframework.stereotype.Repository`respectively.
    
3. **Save `ProductRepository.java`:** Save the file.
    

---

### Part 5: Update the ProductService

Now we will modify `ProductService` to use our new `ProductRepository` instead of the in-memory `List`.

1. **Open `ProductService.java`:** Go back to your `ProductService.java` file.
    
2. **Modify ProductService:** Replace the `List<Product>` and `AtomicLong` with an injected `ProductRepository`, and update the methods to use the repository.
    
    
    ```java
    package com.example.productapi;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import java.util.List;
    import java.util.Optional;
    
    @Service
    public class ProductService {
    
        private final ProductRepository productRepository; // Declare the repository
    
        // Spring will automatically inject the ProductRepository here
        @Autowired
        public ProductService(ProductRepository productRepository) {
            this.productRepository = productRepository;
        }
    
        public List<Product> getAllProducts() {
            return productRepository.findAll(); // Use JpaRepository's findAll()
        }
    
        public Optional<Product> getProductById(Long id) {
            return productRepository.findById(id); // Use JpaRepository's findById()
        }
    
        public Product addProduct(Product product) {
            // ID will be automatically generated by the database for new products
            return productRepository.save(product); // Use JpaRepository's save()
        }
    
        public Optional<Product> updateProduct(Long id, Product updatedProduct) {
            // First, find the existing product
            return productRepository.findById(id)
                           .map(product -> {
                               // Update its fields
                               product.setName(updatedProduct.getName());
                               product.setDescription(updatedProduct.getDescription());
                               product.setPrice(updatedProduct.getPrice());
                               return productRepository.save(product); // Save the updated product
                           });
        }
    
        public boolean deleteProduct(Long id) {
            if (productRepository.existsById(id)) { // Check if it exists before deleting
                productRepository.deleteById(id);   // Use JpaRepository's deleteById()
                return true;
            }
            return false;
        }
    }
    ```
    
    _Self-correction tip:_ Make sure you remove the `AtomicLong` and the constructor that initialized the `ArrayList`. Also, remove `ArrayList` and `AtomicLong` imports if no longer needed. Ensure `Autowired` is imported.
    
3. **Save `ProductService.java`:** Save the file.
    

---

### Part 6: Add Initial Data (Optional but Recommended)

Since our H2 database is in-memory and starts fresh every time, it's helpful to add some initial data when the application launches.

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `DataLoader.java`.
    
2. **Add the DataLoader class:** Copy and paste the following code into `DataLoader.java`:
    
    
    ```java
    package com.example.productapi;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    
    @Configuration // Marks this as a Spring configuration class
    public class DataLoader {
    
        private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    
        // This bean will run once the application context is loaded
        @Bean
        CommandLineRunner initDatabase(ProductRepository repository) {
            return args -> {
                log.info("Preloading " + repository.save(new Product(null, "Laptop", "Powerful laptop for coding.", 1200.00)));
                log.info("Preloading " + repository.save(new Product(null, "Mouse", "Ergonomic wireless mouse.", 25.00)));
                log.info("Preloading " + repository.save(new Product(null, "Keyboard", "Mechanical keyboard with RGB.", 150.00)));
            };
        }
    }
    ```
    
    **Important:** When creating new `Product` objects here, pass `null` for the ID. The database will generate it automatically due to `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
    
3. **Save `DataLoader.java`:** Save the file.
    

---

### Part 7: Testing Your API and H2 Console

Now, let's run the updated application and verify that data is being persisted in the H2 database.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button above the `main` method (or use the Run and Debug view).
        
    - In the "DEBUG CONSOLE" or "TERMINAL", you should see log messages indicating that the H2 database is being initialized and the initial products are being saved. Also, look for SQL statements like `insert into product ...`.
        
    - Ensure the application starts successfully on port 8080.
        
2. **Access H2 Console:**
    
    - Open your web browser.
        
    - Go to: `http://localhost:8080/h2-console`
        
    - On the H2 Console login page:
        
        - **JDBC URL:** Ensure it matches what you put in `application.properties`: `jdbc:h2:mem:productdb`
            
        - **User Name:** `sa`
            
        - **Password:** `password`
            
    - Click "Connect".
        
    - You should now see the H2 database browser. On the left, you'll see a `PRODUCT` table. Click on it, then click "Run" at the top to execute `SELECT * FROM PRODUCT;`. You should see the three preloaded products. This confirms your application is connected to and writing to the H2 database!
        
    - Keep the H2 Console open as you perform API tests.
        

#### Testing with Thunder Client (VS Code)

Use Thunder Client (the lightning bolt icon in the Activity Bar) to send requests.

1. **Create New Request:** Click "New Request".
    
    **a) GET All Products**
    
    - **Method:** `GET`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `200 OK` and a JSON array of your initial three products. Check the H2 console again; it will still show the same data.
        
    
    **b) POST (Create) a New Product**
    
    - **Method:** `POST`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste:
        
        
        ```json
        {
            "name": "Webcam",
            "description": "Full HD webcam for video calls.",
            "price": 75.00
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `201 Created` and the JSON of the new product with its generated ID (e.g., ID 4).
        
        - **Verification:** Go to your H2 Console and re-run `SELECT * FROM PRODUCT;`. You should now see the newly added Webcam!
            
    
    **c) PUT (Update) an Existing Product**
    
    - **Method:** `PUT`
        
    - **URL:** `http://localhost:8080/api/products/1` (Update the Laptop)
        
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste (change price and description):
        
        
        ```json
        {
            "name": "Laptop",
            "description": "Powerful gaming laptop (updated).",
            "price": 1499.99
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `200 OK` and the JSON of the updated product.
        
        - **Verification:** Check the H2 Console for the updated Laptop entry.
            
    
    **d) DELETE a Product**
    
    - **Method:** `DELETE`
        
    - **URL:** `http://localhost:8080/api/products/2` (Delete the Mouse)
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `204 No Content`.
        
        - **Verification:** Check the H2 Console. The Mouse entry should be gone.
            

#### Testing with `curl` Commands (Terminal)

These commands assume your application is running.

---

#### `curl` Appendix for Lab 2


```bash
# Ensure your Spring Boot application is running before executing these.

# 1. GET All Products
echo "--- GET All Products ---"
curl -X GET http://localhost:8080/api/products
echo ""
echo "--------------------------"

# 2. POST (Create) a New Product
#    Check H2 Console after this to see the new entry!
echo "--- POST New Product ---"
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{ "name": "Webcam", "description": "Full HD webcam for video calls.", "price": 75.00 }' \
     http://localhost:8080/api/products
echo ""
echo "--------------------------"

# 3. GET Product by ID (e.g., ID 1)
echo "--- GET Product by ID (ID 1) ---"
curl -X GET http://localhost:8080/api/products/1
echo ""
echo "--------------------------"

# 4. PUT (Update) an Existing Product (e.g., ID 1)
#    Check H2 Console after this to see the update!
echo "--- PUT Update Product (ID 1) ---"
curl -X PUT \
     -H "Content-Type: application/json" \
     -d '{ "name": "Laptop Pro", "description": "Powerful gaming laptop (updated).", "price": 1499.99 }' \
     http://localhost:8080/api/products/1
echo ""
echo "--------------------------"

# 5. DELETE a Product (e.g., ID 2)
#    Check H2 Console after this to see the deletion!
echo "--- DELETE Product (ID 2) ---"
curl -X DELETE http://localhost:8080/api/products/2
echo ""
echo "--------------------------"

# 6. Verify Deletion (Optional: GET all again)
echo "--- GET All Products (after deletion) ---"
curl -X GET http://localhost:8080/api/products
echo ""
echo "--------------------------"

# 7. Test Not Found (e.g., try to get a deleted or non-existent ID)
echo "--- GET Product by non-existent ID (e.g., ID 2 again) ---"
curl -X GET http://localhost:8080/api/products/2
echo ""
echo "--------------------------"
```

---

### Congratulations!

You have successfully transitioned your Spring Boot API to use a real (albeit in-memory) database with Spring Data JPA. This is a massive step towards building production-ready applications, as data persistence is crucial for almost any real-world system. You also learned how to use the H2 console to verify your database operations directly.
