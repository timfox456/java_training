## Lab 4: Basic Spring Security (HTTP Basic Authentication)

**Goal:** In this lab, you will add a layer of security to your Spring Boot REST API, requiring users to authenticate before accessing your product endpoints. You will implement HTTP Basic Authentication with in-memory user details.

**Concepts You'll Learn:**

- How to integrate Spring Security into a Spring Boot application.
    
- The default security behavior applied by Spring Boot.
    
- Configuring in-memory users and roles.
    
- Securing specific URL patterns (`/api/products/**`).
    
- Understanding `UserDetailsService` and `PasswordEncoder`.
    
- Testing secured API endpoints using Thunder Client and `curl` with authentication.
    

**Prerequisites:**

- Completed Lab 3 successfully.
    
- Your `product-api` project from Lab 3.
    

---

### Part 1: Project Setup - Adding Spring Security Dependency

First, let's add the Spring Security dependency to your project.

1. **Open `pom.xml`:** In your `product-api` project in VS Code, open the `pom.xml` file.
    
2. **Add Security Dependency:** Locate the `<dependencies>` section and add the following new dependency:

    
    ```xml
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-security</artifactId>
    		</dependency>
    ```
    
    Your `pom.xml` dependencies section should now include this:
        
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
    			<artifactId>spring-boot-starter-validation</artifactId>
    		</dependency>
    
    		<!-- NEW: Spring Security for authentication and authorization -->
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-security</artifactId>
    		</dependency>
    
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-test</artifactId>
    			<scope>test</scope>
    		</dependency>
    	</dependencies>
    ```
    
3. **Save `pom.xml`:** Save the file (`Ctrl+S` / `Cmd+S`). VS Code will automatically detect the changes and re-import the Maven project, downloading the new dependencies. Wait for this to complete.
    

---

### Part 2: Implement Basic Security Configuration

Now we'll define how users are authenticated and which parts of your application are secured.

1. **Create a new file:** In the `src/main/java/com/example/productapi` directory, create a new file named `SecurityConfig.java`.
    
2. **Add `SecurityConfig` class:** Copy and paste the following code into `SecurityConfig.java`. This configuration defines in-memory users and secures your `/api/products` endpoints.
    

    ```java
    package com.example.productapi;
    
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.provisioning.InMemoryUserDetailsManager;
    import org.springframework.security.web.SecurityFilterChain;
    import static org.springframework.security.config.Customizer.withDefaults; // For httpBasic()
    
    @Configuration // Marks this class as a Spring configuration class
    @EnableWebSecurity // Enables Spring Security's web security features
    public class SecurityConfig {
    
        /**
         * Configures the security filter chain, defining authorization rules.
         * For Spring Boot 3+, this replaces WebSecurityConfigurerAdapter.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authorize -> authorize
                    // Require authentication for all requests to /api/products and its sub-paths
                    .requestMatchers("/api/products/**").authenticated()
                    // Allow access to H2 Console without authentication (for development)
                    .requestMatchers("/h2-console/**").permitAll()
                    // Any other request requires authentication (can be changed to .permitAll() if desired)
                    .anyRequest().authenticated()
                )
                // Use HTTP Basic authentication for secured endpoints
                .httpBasic(withDefaults())
                // Disable CSRF (Cross-Site Request Forgery) protection for simplicity in this API lab.
                // In a real-world web application (especially with browsers), CSRF protection is crucial.
                // For stateless REST APIs, it's often disabled if other security measures are in place.
                .csrf(csrf -> csrf.disable())
                // Configure header options, specifically to allow H2 Console in a frame
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Allow H2 Console to work in an iframe
    
            return http.build();
        }
    
        /**
         * Defines in-memory users for authentication.
         * In a real application, this would typically involve a database (e.g., using Spring Data JPA)
         * and a custom UserDetailsService to retrieve user details.
         */
        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            // Define a 'user' with role "USER"
            UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("password")) // Password encoded
                .roles("USER")
                .build();
    
            // Define an 'admin' with roles "ADMIN" and "USER"
            UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("adminpass")) // Password encoded
                .roles("ADMIN", "USER")
                .build();
    
            // Store users in an in-memory manager
            return new InMemoryUserDetailsManager(user, admin);
        }
    
        /**
         * Defines the password encoder used to encode and verify passwords.
         * BCryptPasswordEncoder is a strong, recommended password hashing function.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    ```
    
    _Self-correction tip:_ Ensure all necessary imports are present. Specifically, `jakarta.servlet.http` imports are implied by `HttpSecurity`. The `withDefaults()` static import is for `Customizer.withDefaults()`.
    
3. **Save `SecurityConfig.java`:** Save the file.
    

---

### Part 3: Testing Your Secured API with Thunder Client (VS Code)

Now, your product API endpoints are protected. Let's test how to access them with and without credentials.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button.
        
    - Ensure the application starts successfully on port 8080. You should see security-related logs during startup.
        
2. **Access H2 Console (Verification):**
    
    - Open your web browser and go to: `http://localhost:8080/h2-console`
        
    - You should be able to connect to the H2 console _without_ providing any credentials, as we configured it to `permitAll()`. This confirms your `permitAll()` rule is working.
        

#### Testing `product-api` Endpoints

Use Thunder Client (the lightning bolt icon in the Activity Bar) to send requests.

1. **Create New Request:** Click "New Request".
    
    **a) Test 1: GET All Products (Unauthorized Access)**
    
    - **Method:** `GET`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Auth:** Select `No Auth`.
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `401 Unauthorized` status. The response body might be empty or contain a simple "Unauthorized" message. This shows the endpoint is now protected.
        
    
    **b) Test 2: GET All Products (Authorized Access - User Role)**
    
    - **Method:** `GET`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Auth:** Select `Basic Auth`.
        
        - **Username:** `user`
            
        - **Password:** `password`
            
    - **Send:** Click "Send".
        
    - **Expected Response:** `200 OK` status and a JSON array containing your products. This confirms successful authentication.
        
    
    **c) Test 3: POST New Product (Authorized Access - Admin Role)**
    
    - **Method:** `POST`
        
    - **URL:** `http://localhost:8080/api/products`
        
    - **Auth:** Select `Basic Auth`.
        
        - **Username:** `admin`
            
        - **Password:** `adminpass`
            
    - **Headers:** `Content-Type: application/json`
        
    - **Body:** Select `JSON` and paste:
    
        ```json
        {
            "name": "Smartwatch",
            "description": "Fitness tracking smartwatch.",
            "price": 250.00
        }
        ```
        
    - **Send:** Click "Send".
        
    - **Expected Response:** `201 Created` status and the JSON of the new product. This confirms `admin` can create products.
        
    
    **d) Test 4: DELETE Product (Authorized Access - Admin Role)**
    
    - **Method:** `DELETE`
        
    - **URL:** `http://localhost:8080/api/products/1` (Assuming ID 1 exists)
        
    - **Auth:** Select `Basic Auth`.
        
        - **Username:** `admin`
            
        - **Password:** `adminpass`
            
    - **Send:** Click "Send".
        
    - **Expected Response:** `204 No Content`. This confirms `admin` can delete products.
        
    
    **e) Test 5: DELETE Product (Authorized Access - User Role - _Optional if time permits, requires role-based method security_)**
    
    - _Note: With the current `SecurityConfig`, both `user` and `admin` can perform all actions on `/api/products/**`because `authenticated()` is used. To restrict `DELETE` only to `admin`, you'd need method-level security (e.g., `@PreAuthorize("hasRole('ADMIN')")` on the controller method) which is a more advanced topic for this lab._
        
    - **For this lab, expect `user` to also be able to delete.**
        
    - **Method:** `DELETE`
        
    - **URL:** `http://localhost:8080/api/products/2` (Assuming ID 2 exists)
        
    - **Auth:** Select `Basic Auth`.
        
        - **Username:** `user`
            
        - **Password:** `password`
            
    - **Send:** Click "Send".
        
    - **Expected Response:** `204 No Content`.
        

---

#### `curl` Appendix for Lab 4 Testing

Remember to replace the placeholder `product-id` in DELETE/PUT commands with an actual ID from your database.

Bash

```
# Ensure your Spring Boot application is running before executing these.

echo "--- Lab 4: Spring Security Tests with curl ---"

# 1. GET All Products (Unauthorized Access)
#    Expected: HTTP 401 Unauthorized
echo "--- GET /api/products (Unauthorized) ---"
curl -v -X GET http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 2. GET All Products (Authorized Access - User Role)
#    Username: user, Password: password
#    Expected: HTTP 200 OK, JSON array of products
echo "--- GET /api/products (Authorized as user) ---"
curl -v -X GET -u user:password http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 3. GET All Products (Authorized Access - Admin Role)
#    Username: admin, Password: adminpass
#    Expected: HTTP 200 OK, JSON array of products
echo "--- GET /api/products (Authorized as admin) ---"
curl -v -X GET -u admin:adminpass http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 4. POST New Product (Authorized Access - Admin Role)
#    Username: admin, Password: adminpass
#    Expected: HTTP 201 Created, JSON of new product
echo "--- POST /api/products (Authorized as admin) ---"
curl -v -X POST \
     -H "Content-Type: application/json" \
     -u admin:adminpass \
     -d '{ "name": "Smartwatch", "description": "Fitness tracking smartwatch.", "price": 250.00 }' \
     http://localhost:8080/api/products
echo ""
echo "----------------------------------------"

# 5. DELETE Product (Authorized Access - Admin Role)
#    Delete product with ID 1. Replace '1' with an existing product ID if needed.
#    Username: admin, Password: adminpass
#    Expected: HTTP 204 No Content
echo "--- DELETE /api/products/1 (Authorized as admin) ---"
curl -v -X DELETE -u admin:adminpass http://localhost:8080/api/products/1
echo ""
echo "----------------------------------------"

# 6. Access H2 Console (Permitted Access)
#    Expected: HTTP 200 OK (HTML content for H2 console login)
echo "--- GET /h2-console (Permitted) ---"
curl -v -X GET http://localhost:8080/h2-console
echo ""
echo "----------------------------------------"
```

---

### Congratulations!

You have successfully implemented basic HTTP Basic Authentication using Spring Security. You've learned how to secure your API endpoints, define in-memory users, and test access with and without proper credentials. This is a crucial step towards building secure and robust enterprise applications.
