## Lab 8: Scheduling Background Tasks

**Goal:** In this lab, you will learn how to implement simple background tasks that run automatically at specified intervals using Spring Boot's scheduling capabilities. This is useful for periodic operations like data cleanup, report generation, or fetching external data.

**Concepts You'll Learn:**

- **`@EnableScheduling`:** The annotation to enable Spring's scheduling functionality in your application.
    
- **`@Scheduled`:** The annotation used to mark a method as a scheduled task.
    
- **Scheduling Types:**
    
    - `fixedRate`: Executes a task at a fixed interval between invocations.
        
    - `fixedDelay`: Executes a task at a fixed interval after the completion of the previous one.
        
    - `cron`: Uses cron expressions for more flexible scheduling (e.g., "every Monday at 9 AM").
        
- **Asynchronous Nature of Scheduling:** How scheduled tasks typically run on separate threads.
    

**Prerequisites:**

- Completed Lab 7 successfully.
    
- Your `product-api` project from Lab 7.
    

---

### Part 1: Enable Scheduling

First, you need to tell your Spring Boot application that you intend to use scheduling.

1. **Open `ProductApiApplication.java`:** In your `product-api` project, open `src/main/java/com/example/productapi/ProductApiApplication.java`.
    
2. **Add `@EnableScheduling`:** Add the `@EnableScheduling` annotation directly above your `ProductApiApplication`class.
    
    
    ```java
    package com.example.productapi;
    
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.scheduling.annotation.EnableScheduling; // NEW
    
    @SpringBootApplication
    @EnableScheduling // NEW: Enables Spring's scheduling capabilities
    public class ProductApiApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(ProductApiApplication.class, args);
        }
    
    }
    ```
    
    _Self-correction tip:_ Ensure `import org.springframework.scheduling.annotation.EnableScheduling;` is added.
    
3. **Save `ProductApiApplication.java`:** Save the file.
    

---

### Part 2: Create a Scheduled Task

Now, let's create a new class that will contain your scheduled methods. This helps keep your code organized. We'll make a simple task that logs the current product count periodically.

1. **Create a new package (optional but good practice):**
    
    - In `src/main/java/com/example/productapi`, create a new folder named `scheduler`. This will correspond to a new package `com.example.productapi.scheduler`.
        
2. **Create `ProductScheduler.java`:**
    
    - In the `src/main/java/com/example/productapi/scheduler` directory, create a new file named `ProductScheduler.java`.
        
    - Add the following code. This class will be a Spring component and use a logger to output messages.
        
    
    
```java
    package com.example.productapi.scheduler;
    
    import com.example.productapi.ProductService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;
    
    import java.time.LocalDateTime;
    
    @Component // Marks this class as a Spring component, so it's picked up for scheduling
    public class ProductScheduler {
    
        private static final Logger logger = LoggerFactory.getLogger(ProductScheduler.class);
    
        private final ProductService productService;
    
        @Autowired
        public ProductScheduler(ProductService productService) {
            this.productService = productService;
        }
    
        /**
         * This method will run every 10 seconds (10000 milliseconds).
         * fixedRate ensures the method is invoked every 10 seconds, regardless of how long the previous execution took.
         * If an execution takes longer than the fixedRate, the next execution will start immediately after the previous one finishes.
         */
        @Scheduled(fixedRate = 10000) // Runs every 10 seconds (10,000 milliseconds)
        public void reportProductCount() {
            long count = productService.getAllProducts().size(); // Get current product count
            logger.info("ProductScheduler - [{}]: Current product count: {}", LocalDateTime.now(), count);
        }
    
        /**
         * Optional: Example of fixedDelay. Runs 15 seconds after the PREVIOUS task COMPLETES.
         * Useful if you need a guaranteed delay between task executions.
         */
        // @Scheduled(fixedDelay = 15000) // Runs 15 seconds AFTER previous run finishes
        // public void cleanupTask() {
        //     logger.info("ProductScheduler - [{}]: Running cleanup task...", LocalDateTime.now());
        //     // Simulate a task that takes some time
        //     try {
        //         Thread.sleep(5000); // Sleep for 5 seconds
        //     } catch (InterruptedException e) {
        //         Thread.currentThread().interrupt();
        //     }
        //     logger.info("ProductScheduler - [{}]: Cleanup task finished.", LocalDateTime.now());
        // }
    
        /**
         * Optional: Example of a cron expression.
         * This example runs every minute at 0 seconds (e.g., 00:00, 01:00, 02:00, etc.)
         * Cron format: second, minute, hour, day of month, month, day of week.
         */
        // @Scheduled(cron = "0 * * * * *") // Every minute at the 0-second mark
        // public void monthlyReportGeneration() {
        //     logger.info("ProductScheduler - [{}]: Running a cron-scheduled task (every minute example).", LocalDateTime.now());
        // }
    }
```
    
    _Self-correction tip:_ Ensure all necessary imports are added: `import org.slf4j.Logger;` `import org.slf4j.LoggerFactory;` `import org.springframework.beans.factory.annotation.Autowired;` `import org.springframework.scheduling.annotation.Scheduled;` `import org.springframework.stereotype.Component;` `import java.time.LocalDateTime;` And `import com.example.productapi.ProductService;` if you created the `scheduler` package.
    
3. **Save `ProductScheduler.java`:** Save the file.
    

---

### Part 3: Observe the Scheduled Task

Now, run your application and observe the console output to see your scheduled task in action.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button.
        
    - Ensure the application starts successfully on port 8080.
        
2. **Observe the Console Output:**
    
    - In the VS Code "DEBUG CONSOLE" or "TERMINAL" where your application is running, you should start seeing log messages from your `ProductScheduler`.
        
    - The message `ProductScheduler - [YYYY-MM-DDTHH:MM:SS.NNN]: Current product count: X` should appear approximately every 10 seconds.
        
    - The `X` value should correspond to the number of products currently in your H2 database (e.g., 3 if you haven't added more, or more if you did so in Lab 5/6).
        
3. **Interact with API (Optional):**
    
    - While the scheduler is running, you can use Thunder Client (or your frontend from Lab 6) to add a new product via `POST /api/products`.
        
    - Observe the console output again. The next scheduled log message should reflect the new total product count!
        

---

#### `curl` Appendix for Lab 8 Testing

There are no direct `curl` commands to _trigger_ a scheduled task, as they run internally within the application. The primary way to observe them is through the application's logs. However, you can use `curl` to interact with your API, and then check the scheduler logs to see how the product count reflects your changes.

```bash
# Ensure your Spring Boot application is running before executing these.
# The scheduled tasks will log output directly to your application's console/terminal.

echo "--- Lab 8: Observing Scheduled Tasks ---"
echo "Scheduled tasks will output to the Spring Boot application console."
echo "You can use the commands below to interact with the API and see count changes."

# Add a new product (assuming you use admin credentials for POST)
echo "--- POST a new product (then observe scheduler output) ---"
curl -v -X POST \
     -H "Content-Type: application/json" \
     -u admin:adminpass \
     -d '{ "name": "Webcam", "description": "High-quality video camera for calls.", "price": 49.99 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# Wait for 10-15 seconds and check your Spring Boot application's console.
# You should see the 'Current product count' log message update with the new total.

# Optionally, delete a product (assuming user credentials can delete, or switch to admin)
# Replace <ID_TO_DELETE> with an actual product ID, e.g., 4 if Webcam was ID 4
echo "--- DELETE a product (then observe scheduler output) ---"
# First, fetch all products to get an ID (if you don't know one)
curl -s -u user:password http://localhost:8080/api/products | json_pp # Requires `json_pp` or similar JSON formatter
# Once you have an ID, e.g., ID 4 for Webcam
# curl -v -X DELETE -u admin:adminpass http://localhost:8080/api/products/4
echo "Delete a product by ID (e.g., ID 4 if you just created Webcam and it got ID 4)"
echo "Example: curl -v -X DELETE -u admin:adminpass http://localhost:8080/api/products/4"
echo ""
echo "----------------------------------------"
# Wait again and check the console. The count should reflect the deletion.
```

---

### Congratulations!

You've successfully implemented your first scheduled task in Spring Boot! You now know how to set up recurring jobs to perform background operations automatically, which is a powerful feature for maintaining and automating aspects of your application.
