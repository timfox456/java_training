## Lab 6: Building a Basic React-less Frontend with Authentication

**Goal:** In this lab, you will create a simple HTML and JavaScript frontend application that interacts with your secured Spring Boot REST API. This frontend will include a basic login mechanism using HTTP Basic Authentication and display the products fetched from your backend service.

**Concepts You'll Learn:**

- **Cross-Origin Resource Sharing (CORS):** Why it's necessary for separate frontend/backend applications and how to enable it in Spring Boot.
    
- **Basic Frontend Structure:** Creating simple HTML, CSS, and JavaScript files.
    
- **HTTP Basic Authentication in JavaScript:** How to send credentials to a secured endpoint.
    
- **Fetching Data:** Making `GET` requests from JavaScript to your API.
    
- **Dynamic HTML Manipulation:** Displaying fetched data in the browser.
    
- **VS Code "Live Server" extension:** A simple way to serve static frontend files locally without Node.js.
    

**Prerequisites:**

- Completed Lab 4 successfully (your Spring Boot application should have security enabled).
    
- Your `product-api` project from Lab 4.
    
- **Recommended:** Restart your Spring Boot application _before_ starting this lab to ensure the H2 database is reset to its initial state (or whatever state you prefer for testing the frontend).
    

---

### Part 1: Backend Preparation (CORS Configuration)

When your frontend (running on `http://127.0.0.1:5500` or `http://localhost:5500` via Live Server) tries to access your backend (running on `http://localhost:8080`), web browsers enforce a security measure called **Cross-Origin Resource Sharing (CORS)**. By default, this prevents a web page from making requests to a different domain, port, or protocol than the one it originated from. You need to tell your Spring Boot application to allow requests from your frontend's origin.

1. **Open `ProductController.java`:** In your `product-api` project, go to `src/main/java/com/example/productapi/ProductController.java`.
    
2. **Add `@CrossOrigin` Annotation:** Add the `@CrossOrigin` annotation directly above your `@RestController`annotation. For this lab, we'll allow all origins (`*`) and all common HTTP methods. **In a production environment, you should be specific about the origins allowed.**

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
    import java.util.List;
    import java.util.stream.Collectors;
    
    @RestController
    @RequestMapping("/api/products")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}) // NEW
    public class ProductController {
    
        private final ProductService productService;
    
        @Autowired
        public ProductController(ProductService productService) {
            this.productService = productService;
        }
    
        // ... (rest of your controller methods, helper methods)
    }
    ```
    
    _Self-correction tip:_ Ensure `import org.springframework.web.bind.annotation.CrossOrigin;` and `import org.springframework.web.bind.annotation.RequestMethod;` are added.
    
3. **Save `ProductController.java`:** Save the file.
    

---

### Part 2: Frontend Project Setup (Static Files)

You'll create a new, separate folder for your frontend application. This keeps it distinct from your Spring Boot backend.

1. **Create a New Folder:** Outside your `product-api` directory (e.g., in your `Downloads` folder, or wherever you keep your labs), create a new folder named `product-frontend`.
    
2. **Open `product-frontend` in a New VS Code Window:**
    
    - Go to `File > New Window` in VS Code.
        
    - In the new VS Code window, go to `File > Open Folder...` and select your newly created `product-frontend` folder. This keeps your frontend and backend projects separate in VS Code, which is good practice.
        
3. **Install VS Code "Live Server" Extension:**
    
    - In your new VS Code window (with `product-frontend` open), go to the Extensions view (the square icon on the left, `Ctrl+Shift+X` or `Cmd+Shift+X`).
        
    - Search for "Live Server" (by Ritwick Dey).
        
    - Click "Install". This extension will allow you to quickly launch a local development server for your static HTML files.
        
4. **Create `index.html`:**
    
    - In your `product-frontend` folder, create a new file named `index.html`.
        
    - Paste the following basic HTML structure:
        
    ```html
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Product Management App</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; color: #333; }
            .container { max-width: 800px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
            h1 { text-align: center; color: #0056b3; }
            #login-section, #product-section { margin-top: 20px; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
            #login-section h2, #product-section h2 { margin-top: 0; color: #333; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input[type="text"], input[type="password"] { width: calc(100% - 22px); padding: 10px; margin-bottom: 10px; border: 1px solid #ddd; border-radius: 4px; }
            button {
                background-color: #007bff;
                color: white;
                padding: 10px 15px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 16px;
                margin-right: 10px;
            }
            button:hover { background-color: #0056b3; }
            #error-message { color: red; margin-top: 10px; text-align: center; }
            #product-list { list-style-type: none; padding: 0; }
            #product-list li { background-color: #e9e9e9; margin-bottom: 8px; padding: 10px; border-radius: 4px; display: flex; justify-content: space-between; align-items: center; }
            #product-list li span { flex-grow: 1; }
            .product-actions { margin-left: 10px; }
            .logout-button { background-color: #dc3545; }
            .logout-button:hover { background-color: #c82333; }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>Product Management Dashboard</h1>
    
            <div id="login-section">
                <h2>Login</h2>
                <label for="username">Username:</label>
                <input type="text" id="username" value="user"> <!-- Pre-fill for convenience -->
                <label for="password">Password:</label>
                <input type="password" id="password" value="password"> <!-- Pre-fill for convenience -->
                <button id="login-button">Login</button>
                <p id="error-message"></p>
            </div>
    
            <div id="product-section" style="display: none;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h2>Products</h2>
                    <button id="logout-button" class="logout-button">Logout</button>
                </div>
                <ul id="product-list">
                    <!-- Products will be dynamically loaded here -->
                </ul>
            </div>
        </div>
    
        <script src="app.js"></script>
    </body>
    </html>
    ```
    
5. **Create `app.js`:**
    
    - In your `product-frontend` folder, create a new file named `app.js`.
        
    - Paste the following JavaScript code. This contains all the logic for login, fetching data, and updating the UI.
        
    
    ```javascript
    document.addEventListener('DOMContentLoaded', () => {
        // DOM Elements
        const loginSection = document.getElementById('login-section');
        const productSection = document.getElementById('product-section');
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const loginButton = document.getElementById('login-button');
        const logoutButton = document.getElementById('logout-button');
        const errorMessage = document.getElementById('error-message');
        const productList = document.getElementById('product-list');
    
        // Configuration
        const API_BASE_URL = 'http://localhost:8080/api/products';
        let authHeader = ''; // This will store our Basic Auth header
    
        // --- Utility Functions ---
    
        // Function to display messages to the user
        function displayMessage(element, message, isError = true) {
            element.textContent = message;
            element.style.color = isError ? 'red' : 'green';
            setTimeout(() => {
                element.textContent = ''; // Clear message after some time
            }, 5000);
        }
    
        // Function to update UI visibility
        function updateUIVisibility(isLoggedIn) {
            if (isLoggedIn) {
                loginSection.style.display = 'none';
                productSection.style.display = 'block';
            } else {
                loginSection.style.display = 'block';
                productSection.style.display = 'none';
            }
        }
    
        // --- API Interaction Functions ---
    
        // Function to fetch products from the backend
        async function fetchProducts() {
            errorMessage.textContent = ''; // Clear previous errors
            productList.innerHTML = ''; // Clear existing list
    
            try {
                // Include the Authorization header for secured endpoint
                const response = await fetch(API_BASE_URL, {
                    method: 'GET',
                    headers: {
                        'Authorization': authHeader
                    }
                });
    
                if (response.ok) {
                    const data = await response.json();
                    // Assuming data.content is the array of products from paginated response
                    const products = data.content || data; // Handle both paginated and non-paginated (just in case)
                    if (products.length === 0) {
                        productList.innerHTML = '<li>No products found.</li>';
                    } else {
                        products.forEach(product => {
                            const listItem = document.createElement('li');
                            listItem.innerHTML = `
                                <span><strong>${product.name}</strong> - $${product.price.toFixed(2)} (${product.description})</span>
                            `;
                            productList.appendChild(listItem);
                        });
                    }
                } else if (response.status === 401) {
                    // Unauthorized - likely session expired or invalid credentials
                    displayMessage(errorMessage, 'Unauthorized. Please log in again.', true);
                    logout(); // Force logout
                } else {
                    const errorData = await response.json();
                    displayMessage(errorMessage, `Failed to fetch products: ${errorData.message || response.statusText}`, true);
                }
            } catch (error) {
                console.error('Error fetching products:', error);
                displayMessage(errorMessage, 'Network error. Could not connect to the server.', true);
            }
        }
    
        // --- Authentication Functions ---
    
        // Handle user login
        async function login() {
            const username = usernameInput.value;
            const password = passwordInput.value;
    
            if (!username || !password) {
                displayMessage(errorMessage, 'Please enter both username and password.');
                return;
            }
    
            // Encode credentials for HTTP Basic Auth
            const credentials = btoa(`${username}:${password}`); // btoa() performs Base64 encoding
            authHeader = `Basic ${credentials}`;
    
            errorMessage.textContent = ''; // Clear previous error messages
    
            try {
                // Attempt to fetch products as a way to verify credentials
                const response = await fetch(API_BASE_URL, {
                    method: 'GET',
                    headers: {
                        'Authorization': authHeader
                    }
                });
    
                if (response.ok) {
                    // Login successful!
                    displayMessage(errorMessage, 'Login successful!', false);
                    updateUIVisibility(true);
                    fetchProducts(); // Fetch products after successful login
                } else if (response.status === 401) {
                    displayMessage(errorMessage, 'Invalid username or password.', true);
                } else {
                    const errorDetails = await response.json();
                    displayMessage(errorMessage, `Login failed: ${errorDetails.message || response.statusText}`, true);
                }
            } catch (error) {
                console.error('Error during login:', error);
                displayMessage(errorMessage, 'Network error during login. Could not connect to the server.', true);
            }
        }
    
        // Handle user logout
        function logout() {
            authHeader = ''; // Clear authentication header
            updateUIVisibility(false);
            usernameInput.value = 'user'; // Reset inputs for next login
            passwordInput.value = 'password';
            productList.innerHTML = ''; // Clear product list on logout
            displayMessage(errorMessage, 'Logged out successfully.', false);
        }
    
        // --- Event Listeners ---
        loginButton.addEventListener('click', login);
        logoutButton.addEventListener('click', logout);
    
        // Initial UI state
        updateUIVisibility(false); // Start with login section visible
    });
    ```
    
    _Self-correction tip:_ Make sure you save both `index.html` and `app.js`.
    

---

### Part 3: Running Your Frontend (Windows and Mac)

You'll use the "Live Server" VS Code extension to run your `index.html` file.

1. **Ensure Spring Boot Backend is Running:**
    
    - In your other VS Code window (or wherever your `product-api` project is open), go to `ProductApiApplication.java`.
        
    - Click the **"Run"** button above the `main` method.
        
    - Make sure the backend starts successfully on `http://localhost:8080`.
        
2. **Launch Live Server for Frontend:**
    
    - In the VS Code window where you have your `product-frontend` folder open, right-click on the `index.html`file in the Explorer.
        
    - Select **"Open with Live Server"**.
        
    - This will open your default web browser to `http://127.0.0.1:5500/index.html` (or a similar port like `5501`, `5502` etc.).
        
    
    **Alternative for Mac/Linux (if Live Server isn't preferred or available):**
    
    - Open your terminal (`Ctrl+Shift+`' in VS Code, or your standalone Terminal app).
        
    - Navigate to your `product-frontend` directory: `cd /path/to/your/product-frontend`
        
    - Run Python's simple HTTP server (requires Python installed, which is common on Mac/Linux):
        
        Bash
        
        ```
        python3 -m http.server 5500
        # or just 'python -m http.server 5500' on some systems
        ```
        
    - Then, manually open your browser to `http://localhost:5500/index.html`.
        

---

### Part 4: Testing the Full Stack Application

Now, interact with your frontend and observe the communication with your backend.

1. **Open Frontend in Browser:** Make sure your `product-frontend` is open in your browser (`http://127.0.0.1:5500/index.html`).
    
2. **Initial State:** You should see the "Login" section.
    
3. **Attempt Login (Correct Credentials):**
    
    - Enter `user` for Username and `password` for Password (these are the in-memory credentials you configured in Lab 4).
        
    - Click the "Login" button.
        
    - **Expected:** The "Login" section should disappear, and the "Products" section should appear, listing the initial products from your H2 database (Laptop, Mouse, Keyboard, etc.). An "Login successful!" message should briefly appear.
        
4. **Attempt Login (Incorrect Credentials):**
    
    - Click "Logout" (if logged in).
        
    - Enter `user` for Username and `wrongpass` for Password.
        
    - Click "Login".
        
    - **Expected:** You should see an "Invalid username or password." error message below the login form, and the "Products" section should not appear.
        
5. **Refresh Browser:**
    
    - If you refresh the browser page, you'll be taken back to the login screen. This is expected because we are not persisting the session on the frontend in this basic lab. You'd need to re-login.
        
6. **Backend Changes:**
    
    - Keep your frontend running.
        
    - Go back to your Spring Boot VS Code window. Add a new product using Thunder Client (with admin credentials, e.g., `POST http://localhost:8080/api/products` with valid JSON).
        
    - Go back to your frontend and click "Login" again. The newly added product should now appear in the list! This shows live data interaction.
        

---

### Congratulations!

You've successfully built a basic full-stack application! You've connected a simple HTML/JavaScript frontend to your secured Spring Boot REST API, implemented HTTP Basic Authentication from the browser, and dynamically displayed data. This is a significant milestone that brings together your backend skills with a tangible user interface.
