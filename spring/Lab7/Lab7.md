## Lab 7: Spring Boot Actuator for Monitoring

**Goal:** In this lab, you will integrate Spring Boot Actuator into your application to gain valuable insights into its runtime behavior, health, and metrics. You'll learn how to expose and interact with various operational endpoints.

**Concepts You'll Learn:**

- **Spring Boot Actuator:** What it is and why it's used for production-ready applications.
    
- **Actuator Endpoints:** Understanding `/health`, `/info`, `/metrics`, `/env`, `/beans`, etc.
    
- **Exposing Endpoints:** How to make specific Actuator endpoints accessible over HTTP.
    
- **Basic Monitoring:** Inspecting application health and collecting runtime metrics.
    
- **Security for Actuator:** How Spring Security protects Actuator endpoints by default.
    

**Prerequisites:**

- Completed Lab 5 successfully.
    
- Your `product-api` project from Lab 5 (with Spring Security and CORS enabled).
    
- **Optional:** Keep your frontend from Lab 6 ready, as you might want to observe its requests affecting Actuator metrics later.
    

---

### Part 1: Adding Spring Boot Actuator Dependency

First, let's add the Actuator dependency to your project.

1. **Open `pom.xml`:** In your `product-api` project in VS Code, open the `pom.xml` file.
    
2. **Add Actuator Dependency:** Locate the `<dependencies>` section and add the following new dependency:

    ```xml
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-actuator</artifactId>
    		</dependency>
    ```
    
    Your `pom.xml` dependencies section should now include this (among others):
    
    XML
    
    ```xml
    	<dependencies>
    		<dependency>
    			<groupId>org.springframework.boot</groupId>
    			<artifactId>spring-boot-starter-actuator</artifactId>
    		</dependency>
    
    		</dependencies>
    ```
    
3. **Save `pom.xml`:** Save the file (`Ctrl+S` / `Cmd+S`). VS Code will automatically detect the changes and re-import the Maven project, downloading the new dependencies. Wait for this to complete.

4. Add the following to the bottom off `resources/application.properties`

```properties

# Actuator
management.endpoints.web.exposure.include=info,health,metrics
info.app.name=Product Management API
info.app.description=A Spring Boot application for managing products
info.app.version=1.0.0
info.contact.email=support@example.com
info.build.timestamp=${maven.build.timestamp} # This will be populated by Maven if configured

```    

---

### Part 2: Exploring Default Actuator Endpoints

By default, Spring Boot Actuator exposes a few endpoints over HTTP, primarily for basic health checks. Since you have Spring Security enabled, these endpoints will also be secured.

1. **Start Your Spring Boot Application:**
    
    - Go to your `ProductApiApplication.java` file.
        
    - Click the **"Run"** button.
        
    - Ensure the application starts successfully on port 8080.
        
2. **Access Default Endpoints (via Browser or Thunder Client):**
    
    - **Attempt in Browser (expect 401 Unauthorized):**
        
        - Open your web browser and go to: `http://localhost:8080/actuator`
            
        - You'll likely get a "Whitelabel Error Page" or a browser login prompt.
            
        - Now try `http://localhost:8080/actuator/health` and `http://localhost:8080/actuator/info`. You should also get a 401 Unauthorized. This confirms Spring Security is protecting them.
            
    - **Access via Thunder Client (with Basic Auth):**
        
        - Open Thunder Client in VS Code.
            
        - **Request 1: GET `/actuator`**
            
            - **Method:** `GET`
                
            - **URL:** `http://localhost:8080/actuator`
                
            - **Auth:** `Basic Auth` (use `user` / `password` or `admin` / `adminpass`)
                
            - **Send.**
                
            - **Expected Response:** `200 OK`. You should see a JSON response listing links to the exposed endpoints, primarily `health` and `info`.
                
        
                ```json
                {
                    "_links": {
                        "self": {
                            "href": "http://localhost:8080/actuator",
                            "templated": false
                        },
                        "health": {
                            "href": "http://localhost:8080/actuator/health",
                            "templated": false
                        },
                        "health-path": {
                            "href": "http://localhost:8080/actuator/health/{*path}",
                            "templated": true
                        },
                        "info": {
                            "href": "http://localhost:8080/actuator/info",
                            "templated": false
                        }
                    }
                }
                ```
                
        - **Request 2: GET `/actuator/health`**
            
            - **Method:** `GET`
                
            - **URL:** `http://localhost:8080/actuator/health`
                
            - **Auth:** `Basic Auth` (`user` / `password`)
                
            - **Send.**
                
            - **Expected Response:** `200 OK`. You'll see JSON indicating the application's status:
    
                ```json
                {
                    "status": "UP"
                }
                ```
                
                (In a real app, this would show more details like database connection status, disk space, etc.)
                
        - **Request 3: GET `/actuator/info`**
            
            - **Method:** `GET`
                
            - **URL:** `http://localhost:8080/actuator/info`
                
            - **Auth:** `Basic Auth` (`user` / `password`)
                
            - **Send.**
                
            - **Expected Response:** `200 OK`. By default, this will likely be an empty JSON object `{}` unless you've added info properties (see optional step below).
                
3. **Optional: Add Custom Info for `/actuator/info`:**
    
    - Open `src/main/resources/application.properties` (or `application.yml`).
        
    - Add some custom information:
        
        Properties
        
        ```
        # application.properties
        info.app.name=Product API Application
        info.app.version=1.0.0
        info.app.description=RESTful API for managing products
        info.developer.name=Your Name
        ```
        
    - **Restart your Spring Boot application** and re-access `http://localhost:8080/actuator/info` via Thunder Client. You should now see your custom info in the JSON response.
        

---

### Part 3: Exposing More Actuator Endpoints

By default, for security reasons, Spring Boot only exposes `health` and `info` over HTTP. To get more insights, you need to explicitly expose other endpoints.

1. **Modify `application.properties` (or `application.yml`):**
    
    - Open `src/main/resources/application.properties`.
        
    - Add the following lines to expose all common endpoints:
        

    
```properties
    # application.properties
    management.endpoints.web.exposure.include=health,info,beans,env,metrics,mappings,httptrace,loggers
    # Alternatively, to expose ALL:
    # management.endpoints.web.exposure.include=*
```
    
    _Self-correction tip:_ Using `*` is convenient for a lab, but in production, you should only expose what's strictly necessary.
    
2. **Save `application.properties` and Restart Application:**
    
    - Save the file and **restart your Spring Boot application**. This is crucial for the configuration changes to take effect.
        
3. **Explore Newly Exposed Endpoints via Thunder Client:**
    
    - **Request 1: GET `/actuator` (again)**
        
        - **Method:** `GET`
            
        - **URL:** `http://localhost:8080/actuator`
            
        - **Auth:** `Basic Auth` (`user` / `password`)
            
        - **Send.**
            
        - **Expected Response:** `200 OK`. The `_links` section should now include links to the newly exposed endpoints like `beans`, `env`, `metrics`, etc.
            
    - **Request 2: GET `/actuator/beans`**
        
        - **Method:** `GET`
            
        - **URL:** `http://localhost:8080/actuator/beans`
            
        - **Auth:** `Basic Auth` (`user` / `password`)
            
        - **Send.**
            
        - **Expected Response:** `200 OK`. A large JSON response detailing all Spring Beans in your application context. This can be useful for debugging dependency issues.
            
    - **Request 3: GET `/actuator/env`**
        
        - **Method:** `GET`
            
        - **URL:** `http://localhost:8080/actuator/env`
            
        - **Auth:** `Basic Auth` (`user` / `password`)
            
        - **Send.**
            
        - **Expected Response:** `200 OK`. A very large JSON response containing all environment properties, including system properties, environment variables, and properties from your `application.properties` (be careful with sensitive info here!).
            

---

### Part 4: Basic Metrics Exploration

The `/actuator/metrics` endpoint provides a powerful way to observe application performance.

1. **Access `/actuator/metrics`:**
    
    - **Method:** `GET`
        
    - **URL:** `http://localhost:8080/actuator/metrics`
        
    - **Auth:** `Basic Auth` (`user` / `password`)
        
    - **Send.**
        
    - **Expected Response:** `200 OK`. A JSON response listing all available metric names (e.g., `jvm.memory.used`, `http.server.requests`, `process.cpu.usage`, `system.cpu.usage`).
        
2. **Drill Down into a Specific Metric:** Let's look at HTTP server request metrics, which tell you about traffic to your API endpoints.
    
    - **Method:** `GET`
        
    - **URL:** `http://localhost:8080/actuator/metrics/http.server.requests`
        
    - **Auth:** `Basic Auth` (`user` / `password`)
        
    - **Send.**
        
    - **Expected Response:** `200 OK`. This response will give you overall statistics for `http.server.requests`. You'll see `measurements` (e.g., `count`, `sum`, `max`) and potentially `availableTags`.
        
    - **Make some API calls:** Use your frontend or Thunder Client to make a few `GET /api/products` requests, or `POST` a new product.
        
    - **Re-run the `/actuator/metrics/http.server.requests` call.** You should see the `count` measurement increase, reflecting the requests you just made.
        
    - **Drill down with tags:** Metrics can be filtered by tags. For example, to see requests specifically for the `/api/products` endpoint:
        
        - **Method:** `GET`
            
        - **URL:** `http://localhost:8080/actuator/metrics/http.server.requests?tag=uri=/api/products`
            
        - **Auth:** `Basic Auth` (`user` / `password`)
            
        - **Send.**
            
        - **Expected Response:** `200 OK`. This response will give you metrics specifically for the `/api/products`URI.
            

---

#### `curl` Appendix for Lab 7 Testing

Remember to replace `<username>` and `<password>` with your actual credentials (e.g., `user:password`).

Bash

```
# Ensure your Spring Boot application is running before executing these.
# Replace user:password with your actual credentials (e.g., admin:adminpass if user:password doesn't have access to some endpoints)

echo "--- Lab 7: Spring Boot Actuator Tests with curl ---"

# 1. GET Base Actuator Endpoint
#    Expected: HTTP 200 OK, links to exposed endpoints.
echo "--- GET /actuator ---"
curl -v -X GET -u user:password http://localhost:8080/actuator
echo ""
echo "----------------------------------------"

# 2. GET Health Endpoint
#    Expected: HTTP 200 OK, {"status": "UP"}.
echo "--- GET /actuator/health ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/health
echo ""
echo "----------------------------------------"

# 3. GET Info Endpoint (after adding custom info in application.properties)
#    Expected: HTTP 200 OK, custom info if configured.
echo "--- GET /actuator/info ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/info
echo ""
echo "----------------------------------------"

# 4. GET Beans Endpoint (after exposing in application.properties)
#    Expected: HTTP 200 OK, long JSON of all Spring beans.
echo "--- GET /actuator/beans ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/beans
echo ""
echo "----------------------------------------"

# 5. GET Environment Endpoint (after exposing in application.properties)
#    Expected: HTTP 200 OK, very long JSON of environment properties.
echo "--- GET /actuator/env ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/env
echo ""
echo "----------------------------------------"

# 6. GET Metrics List Endpoint (after exposing in application.properties)
#    Expected: HTTP 200 OK, list of available metric names.
echo "--- GET /actuator/metrics ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/metrics
echo ""
echo "----------------------------------------"

# 7. GET Specific Metric: HTTP Server Requests
#    Expected: HTTP 200 OK, details about HTTP request counts, sums, etc.
echo "--- GET /actuator/metrics/http.server.requests ---"
curl -v -X GET -u user:password http://localhost:8080/actuator/metrics/http.server.requests
echo ""
echo "----------------------------------------"

# 8. GET Specific Metric with Tag: HTTP Server Requests for /api/products URI
#    Expected: HTTP 200 OK, details specifically for /api/products.
echo "--- GET /actuator/metrics/http.server.requests?tag=uri=/api/products ---"
curl -v -X GET -u user:password "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri=/api/products"
echo ""
echo "----------------------------------------"
```

---

### Congratulations!

You've successfully integrated Spring Boot Actuator and begun exploring its powerful monitoring capabilities. You now have tools to inspect the health, configuration, and performance of your application at runtime, which is invaluable for operating applications in any environment.
