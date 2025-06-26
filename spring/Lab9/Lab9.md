## Lab 9: Externalizing Configuration and Spring Profiles

**Goal:** In this lab, you will learn how to externalize your application's configuration, making it easy to change settings without recompiling code. You'll also explore Spring Profiles, a powerful mechanism to apply different configurations and component behaviors based on the active environment.

**Concepts You'll Learn:**

- **Externalized Configuration:** The benefits of keeping configuration separate from code.
    
- **`application.properties` vs. `application.yml`:** Understanding the two primary file formats for Spring Boot configuration.
    
- **Custom Properties:** Defining and injecting your own application-specific properties.
    
- **Spring Profiles:** What they are and how they allow environment-specific configurations.
    
- **Activating Profiles:** How to select an active profile via configuration files or command-line arguments.
    
- **Profile-Specific Beans:** Using `@Profile` to activate components only for certain environments.
    

**Prerequisites:**

- Completed Lab 8 successfully.
    
- Your `product-api` project from Lab 8.
    

---

### Part 1: Basic External Configuration (`application.yml`)

We'll start by adding a simple custom property to your application and demonstrating how to inject and use it. We'll use `application.yml` for this lab, as it's often preferred for its hierarchical structure and readability.

1. **Delete `application.properties` (or rename it):**
    
    - In `src/main/resources`, if you have an `application.properties` file, **delete it** or rename it to something like `application.properties.bak`. This ensures Spring Boot prioritizes the `.yml` file.
        
2. **Create `application.yml`:**
    
    - In `src/main/resources`, create a new file named `application.yml`.
        
    - Paste the following content. This includes your existing server port and H2 settings, plus a new custom property:
        

    ```yaml
    # src/main/resources/application.yml
    server:
      port: 8080
    
    spring:
      h2:
        console:
          enabled: true
          path: /h2-console
      datasource:
        url: jdbc:h2:mem:productdb
        username: sa
        password:
        driverClassName: org.h2.Driver
      jpa:
        hibernate:
          ddl-auto: update
        show-sql: true
      # Configuration for Actuator endpoints
      main:
        allow-bean-definition-overriding: true # Allows overriding beans if defined in profiles
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,beans,env,metrics,mappings,httptrace,loggers # From Lab 7
    
    # Custom Application Properties
    app:
      message: Welcome to the Product API (Default Environment)!
      api-version: 1.0.0
    ```
    
    _Self-correction tip:_ Ensure proper YAML indentation (spaces, not tabs).
    
3. **Create a Configuration Component:** Let's create a simple component to read and display these properties.
    
    - In `src/main/java/com/example/productapi`, create a new package named `config`.
        
    - Inside `src/main/java/com/example/productapi/config`, create a new file named `AppConfig.java`.
        

    ```java
    package com.example.productapi.config;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.stereotype.Component;
    
    @Component // Mark as a Spring component to be managed by the Spring context
    public class AppConfig implements CommandLineRunner {
    
        private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    
        // Inject custom properties using @Value
        @Value("${app.message}") // Property name from application.yml
        private String appMessage;
    
        @Value("${app.api-version}")
        private String apiVersion;
    
        @Override
        public void run(String... args) throws Exception {
            // This method runs once the application context has loaded
            logger.info("Application Configuration Loaded:");
            logger.info("  App Message: {}", appMessage);
            logger.info("  API Version: {}", apiVersion);
        }
    }
    ```
    
    _Self-correction tip:_ Ensure `import` statements are correct. `CommandLineRunner` is a convenient way to run code after the Spring application starts.
    
4. **Save all files.**
    
5. **Run and Verify:**
    
    - **Run your Spring Boot application** from `ProductApiApplication.java`.
        
    - Observe the **console output**. You should see the log messages from `AppConfig` displaying the values "Welcome to the Product API (Default Environment)!" and "1.0.0".
        

---

### Part 2: Introducing Spring Profiles

Spring Profiles allow you to define different configurations for different environments (e.g., `dev` for development, `test`for testing, `prod` for production).

1. **Create Profile-Specific Configuration Files:**
    
    - In `src/main/resources`, create two new files:
        
        - `application-dev.yml`
            
        - `application-test.yml`
            
    - **Content for `application-dev.yml`:**
        
        YAML
        
        ```
        # src/main/resources/application-dev.yml
        app:
          message: Welcome to the Product API (Development Environment)!
          api-version: 1.0.0-DEV
        ```
        
    - **Content for `application-test.yml`:**
        
        YAML
        
        ```
        # src/main/resources/application-test.yml
        app:
          message: Welcome to the Product API (Testing Environment)!
          api-version: 1.0.0-TEST
        ```
        
    
    _Self-correction tip:_ Again, ensure correct YAML indentation.
    
2. **Activate Profiles:** There are several ways to activate profiles.
    
    **Method A: Activate via `application.yml` (e.g., for default local dev)**
    
    - Open your main `src/main/resources/application.yml` file.
        
    - Add `spring.profiles.active` to set the default active profile:
        

        ```yaml
        # src/main/resources/application.yml
        server:
          port: 8080
        # ... (other existing configurations) ...
        
        spring:
          profiles:
            active: dev # This will activate the 'dev' profile by default
        # ... (rest of the file) ...
        ```
        
    - **Save `application.yml` and Restart your application.**
        
    - **Observe:** The console should now show messages like: `App Message: Welcome to the Product API (Development Environment)!` `API Version: 1.0.0-DEV` This means `application-dev.yml` properties are overriding the `application.yml` ones.
        
    
    **Method B: Activate via Command Line (Overrides `application.yml`)** This is often used in CI/CD pipelines or when running directly from the terminal.
    
    - **Stop your Spring Boot application** (if it's still running from Method A).
        
    - Open your VS Code **TERMINAL** (go to `View > Terminal` or `Ctrl+`' ).
        
    - Navigate to your `product-api` project root directory (where `pom.xml` is).
        
    - Run the application using Maven with the `spring.profiles.active` system property:
        
        Bash
        
        ```
        # For Windows Command Prompt/PowerShell:
        mvn spring-boot:run -Dspring.profiles.active=test
        
        # For macOS/Linux (or Git Bash on Windows):
        ./mvnw spring-boot:run -Dspring.profiles.active=test
        ```
        
    - **Observe:** The console output should now show messages from the `test` profile: `App Message: Welcome to the Product API (Testing Environment)!` `API Version: 1.0.0-TEST`
        
    - **Note:** The command-line argument `-Dspring.profiles.active=test` **takes precedence** over `spring.profiles.active: dev` specified in `application.yml`. This is crucial for environment management.
        

---

### Part 3: Profile-Specific Beans

You can also make entire components (Spring beans) active only when a specific profile is enabled.

1. **Create Profile-Specific Data Initializers:** Let's create two simple components that log a message, and activate them with different profiles.
    
    - In `src/main/java/com/example/productapi`, create a new package named `profiledemo`.
        
    - Inside `src/main/java/com/example/productapi/profiledemo`, create `DevDataInitializer.java`:
        

    ```java
    package com.example.productapi.profiledemo;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.context.annotation.Profile;
    import org.springframework.stereotype.Component;
    
    import jakarta.annotation.PostConstruct; // For @PostConstruct
    
    @Component
    @Profile("dev") // This bean will only be active when the 'dev' profile is active
    public class DevDataInitializer {
    
        private static final Logger logger = LoggerFactory.getLogger(DevDataInitializer.class);
    
        @PostConstruct
        public void init() {
            logger.info("DEV Profile Active: Running development data initialization logic.");
            // In a real app, this might load dummy data for dev, or connect to a dev DB.
        }
    }
    ```
    
    _Self-correction tip:_ Ensure `import jakarta.annotation.PostConstruct;` is added.
    
    - Inside `src/main/java/com/example/productapi/profiledemo`, create `TestEnvironmentLogger.java`:
        
    ```java
    package com.example.productapi.profiledemo;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.context.annotation.Profile;
    import org.springframework.stereotype.Component;
    
    import jakarta.annotation.PostConstruct;
    
    @Component
    @Profile("test") // This bean will only be active when the 'test' profile is active
    public class TestEnvironmentLogger {
    
        private static final Logger logger = LoggerFactory.getLogger(TestEnvironmentLogger.class);
    
        @PostConstruct
        public void init() {
            logger.info("TEST Profile Active: Logging for test environment setup.");
            // In a real app, this might set up test-specific configurations or mocks.
        }
    }
    ```
    
    _Self-correction tip:_ Ensure `import jakarta.annotation.PostConstruct;` is added.
    
2. **Save all new files.**
    
3. **Run with Different Profiles and Observe:**
    
    - **Run with `dev` profile:**
        
        - Ensure `spring.profiles.active: dev` is uncommented in `application.yml` and no command-line arguments are used.
            
        - **Run your Spring Boot application** from `ProductApiApplication.java`.
            
        - **Observe:** You should see `App Message: ...Development Environment!` and **"DEV Profile Active: Running development data initialization logic."** in the console. You should _not_ see the "TEST Profile Active" message.
            
    - **Run with `test` profile (via command line):**
        
        - **Stop your application.**
            
        - In your VS Code TERMINAL, run:
            
    
            ```bash
            # Windows:
            mvn spring-boot:run -Dspring.profiles.active=test
            
            # macOS/Linux:
            ./mvnw spring-boot:run -Dspring.profiles.active=test
            ```
            
        - **Observe:** You should now see `App Message: ...Testing Environment!` and **"TEST Profile Active: Logging for test environment setup."** in the console. You should _not_ see the "DEV Profile Active" message.
            

---

#### `curl` Appendix for Lab 9 Testing

Testing for this lab is primarily about observing the **console output** when you run your application with different configurations or active profiles. There are no direct API calls to demonstrate the profile changes, as they affect the internal loading of properties and beans.

However, you can make a `GET /actuator/env` call (with basic auth, e.g., `user:password`) to actually see the active profiles and loaded properties from your running application:

Bash

```
# Ensure your Spring Boot application is running with a specific profile activated.
# Replace user:password with your actual credentials.

echo "--- Lab 9: Verification with Actuator /env Endpoint ---"

# 1. Activate 'dev' profile (e.g., by setting spring.profiles.active: dev in application.yml)
#    Then run your application from VS Code or:
#    mvn spring-boot:run

#    After it's running, query the environment:
echo "--- Query /actuator/env with 'dev' profile active ---"
curl -s -u user:password http://localhost:8080/actuator/env | json_pp | grep -E "app.message|activeProfiles"
echo ""
echo "Expected output should show 'activeProfiles': ['dev'] and 'app.message' from dev profile."
echo "----------------------------------------"

# 2. Activate 'test' profile (e.g., by running from terminal with -Dspring.profiles.active=test)
#    mvn spring-boot:run -Dspring.profiles.active=test

#    After it's running, query the environment:
echo "--- Query /actuator/env with 'test' profile active ---"
curl -s -u user:password http://localhost:8080/actuator/env | json_pp | grep -E "app.message|activeProfiles"
echo ""
echo "Expected output should show 'activeProfiles': ['test'] and 'app.message' from test profile."
echo "----------------------------------------"

# You can manually scroll through the full 'curl -u user:password http://localhost:8080/actuator/env | json_pp' output
# to see all the properties loaded under each profile.
```

---

### Congratulations!

You've mastered the critical concepts of externalized configuration and Spring Profiles! You now know how to tailor your application's behavior and settings for different environments without modifying your codebase, which is a fundamental skill for deploying and managing enterprise applications.
