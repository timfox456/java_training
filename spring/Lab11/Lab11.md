## Lab 11: Containerizing Your Spring Boot Application with Docker

**Goal:** In this lab, you will learn the basics of containerizing your Spring Boot application using Docker. This involves packaging your application into a lightweight, portable container image that can run consistently across different environments.

**Important Note for Students:**

- This lab introduces **Docker**, which is an external tool. While we'll provide installation instructions, setting up Docker Desktop can sometimes be challenging depending on your operating system configuration (e.g., Windows Subsystem for Linux (WSL) 2 requirements on Windows).
    
- **If you are unable to install or run Docker Desktop on your machine, please do not worry.** This lab is designed to be self-contained. You can still follow along with the steps, understand the concepts, and observe the demonstration if your instructor is running it. Your existing Spring Boot project will _not_ be broken, and all future labs will assume you are running your application locally as before (using `mvn spring-boot:run`).
    

**Concepts You'll Learn:**

- **Containers and Docker:** A brief overview of what they are and why they are used.
    
- **Docker Desktop:** The tool to run Docker on your local machine.
    
- **Building Docker Images:** Packaging your Spring Boot application into a Docker image.
    
- **Running Docker Containers:** Launching your application inside a Docker container.
    
- **Port Mapping:** Connecting the container's port to your host machine's port.
    
- **Basic Docker Commands:** `docker build`, `docker run`, `docker ps`, `docker stop`, `docker rm`.
    

**Prerequisites:**

- Completed Lab 9 successfully.
    
- Your `product-api` project from Lab 9.
    

---

### Part 1: Installing Docker Desktop

Docker Desktop is the easiest way to get Docker running on Windows and Mac.

#### For Windows Users:

1. **Check Prerequisites:**
    
    - **Windows 10/11 64-bit:** Home or Pro version.
        
    - **WSL 2 Feature:** Docker Desktop for Windows requires the Windows Subsystem for Linux 2 (WSL 2) feature to be enabled and installed.
        
        - **To Check/Enable WSL 2:**
            
            - Open **PowerShell as Administrator**.
                
            - Run: `wsl --install` (This command will install WSL, the Linux kernel, and Ubuntu by default).
                
            - If WSL is already installed, ensure it's WSL 2 by running: `wsl -l -v`. Look for "Version 2". If it's Version 1, you might need to update it (search "Upgrade WSL 1 to WSL 2" if needed).
                
            - **Restart your computer** after installing/updating WSL 2.
                
2. **Download Docker Desktop:**
    
    - Go to the official Docker Desktop download page: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
        
    - Click "Download for Windows".
        
3. **Install Docker Desktop:**
    
    - Locate the downloaded installer (e.g., `Docker Desktop Installer.exe`).
        
    - **Double-click** the installer to run it.
        
    - Follow the on-screen instructions. Make sure "Install required Windows components for WSL 2" is checked.
        
    - Once the installation is complete, Docker Desktop will ask you to log in or create a Docker ID (this is optional for local use, you can skip it initially).
        
4. **Start and Verify Docker Desktop:**
    
    - Docker Desktop should start automatically after installation. If not, find it in your Start Menu and launch it.
        
    - The Docker icon (a whale with containers) should appear in your system tray (bottom right). It might take a moment to start and show a green light (or "Docker Desktop is running").
        
    - **Open a new Command Prompt or PowerShell window.**
        
    - Run: `docker --version`
        
    - Run: `docker run hello-world`
        
        - If `hello-world` runs successfully (you'll see a message like "Hello from Docker!"), Docker Desktop is working!
            

#### For Mac Users:

1. **Check Prerequisites:**
    
    - **macOS Monterey 12 or newer.**
        
    - **Intel or Apple Silicon (M1/M2/M3) chip.** Ensure you download the correct installer for your chip type.
        
2. **Download Docker Desktop:**
    
    - Go to the official Docker Desktop download page: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
        
    - Click "Download for Mac" and choose the correct version (Intel or Apple Chip).
        
3. **Install Docker Desktop:**
    
    - Locate the downloaded `.dmg` file.
        
    - **Double-click** the `.dmg` file to open it.
        
    - Drag the Docker icon to the "Applications" folder.
        
    - Close the installation window and eject the `.dmg` file.
        
4. **Start and Verify Docker Desktop:**
    
    - Go to your Applications folder and **double-click "Docker"** to launch it.
        
    - You might be prompted to grant Docker Desktop access. Provide your macOS password if requested.
        
    - The Docker icon (a whale with containers) should appear in your menu bar (top right). It might take a moment to start and show a green light (or "Docker Desktop is running").
        
    - **Open a new Terminal window.**
        
    - Run: `docker --version`
        
    - Run: `docker run hello-world`
        
        - If `hello-world` runs successfully (you'll see a message like "Hello from Docker!"), Docker Desktop is working!
            

---

### Part 2: Prepare Spring Boot for Docker Image Building

Spring Boot provides a fantastic Maven plugin that can directly build Docker images, often called "executable JARs" or "cloud native buildpacks." This saves us from writing a manual `Dockerfile` for this introductory lab.

1. **Open `pom.xml`:** In your `product-api` project in VS Code, open the `pom.xml` file.
    
2. **Add `spring-boot-maven-plugin` configuration:** Locate the `<build>` section in your `pom.xml`. Inside the `<plugins>` section, find the `spring-boot-maven-plugin` configuration. We'll add an `<executions>` block to configure it to build the image.

    ```xml
    	<build>
    		<plugins>
    			<plugin>
    				<groupId>org.springframework.boot</groupId>
    				<artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <image>
                            <name>${project.artifactId}:${project.version}</name>
                            <!-- Optional: Set specific builder or run image. Default is fine for lab. -->
                            <!-- <builder>paketobuildpacks/builder-jammy-base:latest</builder> -->
                            <!-- <pullPolicy>IF_NOT_PRESENT</pullPolicy> -->
                        </image>
                    </configuration>
    				<executions>
    					<execution>
    						<goals>
    							<goal>repackage</goal>
    							<goal>build-info</goal>
    						</goals>
    					</execution>
    				</executions>
    			</plugin>
    		</plugins>
    	</build>
    ```
    
    **Explanation:**
    
    - The `repackage` goal already exists and creates your executable JAR.
        
    - We added the `<configuration>` block for the `<image>` element.
        
    - `<name>${project.artifactId}:${project.version}</name>`: This tells the plugin to name your Docker image using your project's artifact ID and version from `pom.xml` (e.g., `product-api:0.0.1-SNAPSHOT`).
        
3. **Save `pom.xml`:** Save the file (`Ctrl+S` / `Cmd+S`). VS Code will re-import Maven.
    

---

### Part 3: Build the Docker Image

Now that Docker Desktop is running and your `pom.xml` is configured, you can build your Docker image.

1. **Open VS Code Terminal:** Go to `View > Terminal` or press `Ctrl+`' (Windows/Linux) / `Cmd+`' (macOS).
    
2. **Navigate to Project Root:** Ensure your terminal is in the `product-api` project root directory (where `pom.xml` is).
    
3. **Build the Project JAR:** This step compiles your code and creates the executable JAR file.
    
    Bash
    
```bash
    mvn clean install
```
    
    - Wait for this command to complete successfully. You should see `BUILD SUCCESS` at the end.
        
4. **Build the Docker Image:** This command uses the `spring-boot-maven-plugin` to create the Docker image.
    

    ```bash
    mvn spring-boot:build-image
    ```
    
    - This command will take a few minutes for the first time, as it downloads necessary build layers (called "buildpacks") for your application.
        
    - You'll see a lot of output, including messages about "Building image" and "Successfully built image".
        
    - Look for the line that says `Successfully built image 'docker.io/library/product-api:0.0.1-SNAPSHOT'`. (The exact version will match your `pom.xml`).
        
    - **Verify Image:** You can list your Docker images to confirm it was created:
        
        Bash
        
    ```bash
        docker images
    ```
        
        You should see `product-api` in the list.
        

---

### Part 4: Run the Docker Container

Once the image is built, you can run it as a container.

1. **Run the Container:** Use the `docker run` command with port mapping.

    ```bash
    docker run -p 8080:8080 product-api:0.0.1-SNAPSHOT
    ```
    
    - **Explanation:**
        
        - `-p 8080:8080`: This maps port `8080` on your host machine to port `8080` inside the Docker container. This is how you access the application running inside the container from your browser or client tools.
            
        - `product-api:0.0.1-SNAPSHOT`: This is the name and tag of the Docker image you just built (replace with your actual version if different).
            
    - The container will start, and you'll see your Spring Boot application's logs in the terminal, just as if you ran it locally.
        
2. **Verify API Access:** With the container running, use Thunder Client or `curl` to access your API (the same way you did in previous labs). Remember to include authentication.
    
    - **Thunder Client:**
        
        - **Method:** `GET`
            
        - **URL:** `http://localhost:8080/api/products`
            
        - **Auth:** `Basic Auth` (`user:password` or `admin:adminpass`)
            
        - **Send.**
            
        - **Expected:** `200 OK` and a JSON response with your products.
            
    - **`curl` (macOS/Linux / Git Bash):**
        ```bash
        curl -v -X GET -u user:password http://localhost:8080/api/products
        ```
        
        - **Expected:** HTTP `200 OK` and the JSON data.
            
    - **H2 Console:** You can also access the H2 Console in your browser at `http://localhost:8080/h2-console`(no auth needed for H2 console itself, as configured in Lab 4).
        

**Important:** If your local Spring Boot application from Lab 10 is _still running_, you'll get a "Port 8080 already in use" error when trying to run the Docker container. Make sure to stop any locally running instances first (as described in the Lab 10 troubleshooting).

---

### Part 5: Clean Up Docker Resources

It's important to know how to stop and remove Docker containers and images to free up resources.

1. **Stop the Running Container:**
    
    - In the terminal where your Docker container is running (the one showing Spring Boot logs), press `Ctrl+C`. This will gracefully stop the container.
        
    - If `Ctrl+C` doesn't work, you'll need to find the Container ID or Name. Open a _new_ terminal window and run:

        ```bash
        docker ps
        ```
        
        Look for your `product-api` container. Note its `CONTAINER ID`. Then run:

        ```bash
        docker stop <CONTAINER_ID>
        ```
        
2. **Remove the Container (Optional, but Recommended):** After stopping, the container still exists (it's just not running). To remove it:
    
    - First, make sure it's stopped (`docker ps -a` shows all containers, even stopped ones).
        
    - Get its `CONTAINER ID` from `docker ps -a`.
        
    - Run:

        ```bash
        docker rm <CONTAINER_ID>
        ```
        
3. **Remove the Docker Image (Optional):** If you want to completely clean up the image from your local Docker registry:
    
    - First, ensure no containers are using the image (`docker ps -a` should not show any containers from this image).
        
    - Get the `IMAGE ID` or `REPOSITORY:TAG` from `docker images`.
        
    - Run:
        
        ```bash
        docker rmi product-api:0.0.1-SNAPSHOT
        ```
        
        (Replace with your actual image name and tag).
        

---

### Congratulations!

You've successfully containerized your Spring Boot application! You've installed Docker Desktop, built a Docker image for your API, and run it inside a container. This is a crucial step towards understanding modern deployment strategies and makes your application highly portable.

**Remember:** Your original Spring Boot project is unchanged by this lab, and you can continue running it locally using `mvn spring-boot:run` for future labs.
