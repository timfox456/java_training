# Spring Boot Backend Development Labs

Welcome to the Spring Boot Backend Development Labs repository! This section of the `java_training` repository provides a structured learning path for building robust, scalable, and secure backend applications using the Spring Boot framework.

Each lab is designed to introduce core concepts and best practices in a hands-on manner, building incrementally on the knowledge gained from previous labs. **The detailed instructions for implementing each lab are provided within their respective `.md` files.**

## Table of Contents

* [Introduction](#introduction)
* [Labs](#labs)
    * [Lab 1: Initial Spring Boot Project Setup](#lab-1-initial-spring-boot-project-setup)
    * [Lab 2: RESTful API Basics (Products)](#lab-2-restful-api-basics-products)
    * [Lab 3: Data Persistence with H2 Database](#lab-3-data-persistence-with-h2-database)
    * [Lab 4: Spring Security (Basic Authentication)](#lab-4-spring-security-basic-authentication)
    * [Lab 5: Input Validation with JSR 380 (Jakarta Bean Validation)](#lab-5-input-validation-with-jsr-380-jakarta-bean-validation)
    * [Lab 6: Basic Frontend with Authentication](#lab-6-basic-frontend-with-authentication)
    * [Lab 7: Spring Boot Actuator for Monitoring](#lab-7-spring-boot-actuator-for-monitoring)
    * [Lab 8: Scheduling Background Tasks](#lab-8-scheduling-background-tasks)
    * [Lab 9: Externalizing Configuration and Spring Profiles](#lab-9-externalizing-configuration-and-spring-profiles)
    * [Lab 10: Asynchronous Processing with `@Async`](#lab-10-asynchronous-processing-with-async)
    * [Lab 11: Containerizing Your Spring Boot Application with Docker](#lab-11-containerizing-your-spring-boot-application-with-docker)
* [General Execution Notes](#general-execution-notes)
* [Tools Used](#tools-used)

## Introduction

This series of labs guides you through the process of developing a Spring Boot RESTful API from scratch. You'll cover essential topics such as setting up a project, defining API endpoints, integrating a database, implementing security, handling input validation, adding monitoring capabilities, scheduling tasks, managing configurations for different environments, enabling asynchronous operations, and finally, containerizing your application with Docker.

Each lab includes detailed steps and explanations to help you understand the core concepts. By completing these labs, you will build a functional Spring Boot backend application.

## Labs

### [Lab 1: Initial Spring Boot Project Setup](./Lab1/Lab1.md)

* **Description:** This lab covers the very first steps of creating a basic Spring Boot project using Spring Initializr, importing it into your IDE (e.g., VS Code), and running a simple application to ensure basic setup.
* **Concepts:** Project generation, Maven dependencies, main application class, basic Spring Boot application execution.
* **How to Run (conceptual):** `mvn spring-boot:run` (after project creation)

### [Lab 2: RESTful API Basics (Products)](./Lab2/Lab2.md)

* **Description:** You'll build the foundation of your RESTful API by defining a `Product` entity, setting up data access with a repository, implementing business logic in a service layer, and creating a REST controller to expose CRUD (Create, Read, Update, Delete) operations for products.
* **Concepts:** REST principles, Spring Data JPA, `Repository`, `Service`, `Controller` layers, DTOs (Data Transfer Objects).
* **How to Test (conceptual):** Use API client tools (like Thunder Client or `curl`) to send HTTP requests (GET, POST, PUT, DELETE) to your API endpoints.

### [Lab 3: Data Persistence with H2 Database](./Lab3/Lab3.md)

* **Description:** This lab integrates the H2 in-memory database with your Spring Boot application, allowing your product data to be stored and retrieved during the application's runtime. You'll also learn how to access the H2 console for database inspection.
* **Concepts:** In-memory databases, JPA configuration, data source setup, database console access.
* **How to Test (conceptual):** Access the H2 Console in your browser and verify data persistence during application runtime.

### [Lab 4: Spring Security (Basic Authentication)](./Lab4/Lab4.md)

* **Description:** You'll secure your REST API using Spring Security with HTTP Basic Authentication. This ensures that only authenticated users with proper roles can access your endpoints, enhancing the security of your application.
* **Concepts:** Spring Security setup, HTTP Basic Authentication, in-memory user configuration, role-based access control.
* **How to Test (conceptual):** Send HTTP requests with and without authentication headers using an API client to observe access control.

### [Lab 5: Input Validation with JSR 380 (Jakarta Bean Validation)](./Lab5/Lab5.md)

* **Description:** Implement robust input validation for your API's request bodies using Jakarta Bean Validation (JSR 380) annotations. This ensures that incoming data meets your application's requirements before processing, preventing common data errors.
* **Concepts:** Request body validation, `@Valid` annotation, validation constraints (e.g., `@NotBlank`, `@Min`, `@NotNull`), error handling for invalid input.
* **How to Test (conceptual):** Send `POST` or `PUT` requests with invalid data (e.g., empty names, negative prices) using an API client to observe validation errors in the responses.

### [Lab 6: Basic Frontend with Authentication](./Lab6/Lab6.md)

* **Description:** This lab introduces a very basic HTML/JavaScript frontend that interacts with your secured Spring Boot API. You'll learn how to handle Cross-Origin Resource Sharing (CORS) and make authenticated API calls from a web browser.
* **Concepts:** Frontend-backend communication, JavaScript `fetch` API, CORS (Cross-Origin Resource Sharing), basic web authentication flow.
* **How to Test (conceptual):** Run the frontend in a web browser and interact with the login and product display features, observing network requests.

### [Lab 7: Spring Boot Actuator for Monitoring](./Lab7/Lab7.md)

* **Description:** Integrate Spring Boot Actuator to add production-ready monitoring and management endpoints to your application. This lab shows you how to check application health, view runtime metrics, inspect the application context, and more.
* **Concepts:** Application monitoring, Actuator endpoints (`/health`, `/info`, `/metrics`, `/beans`, `/env`), exposing endpoints, securing Actuator.
* **How to Test (conceptual):** Use an API client to access Actuator endpoints (e.g., `/actuator/health`, `/actuator/metrics`) with appropriate authentication.

### [Lab 8: Scheduling Background Tasks](./Lab8/Lab8.md)

* **Description:** Learn how to implement simple background tasks that run automatically at specified intervals using Spring's `@Scheduled` annotation. This is useful for periodic operations like data reporting, cleanup, or data synchronization.
* **Concepts:** Task scheduling, `@EnableScheduling`, `@Scheduled` annotation, `fixedRate`, `fixedDelay`, cron expressions.
* **How to Test (conceptual):** Run your application and observe the console output for periodic log messages generated by the scheduled tasks.

### [Lab 9: Externalizing Configuration and Spring Profiles](./Lab9/Lab9.md)

* **Description:** This lab teaches you to manage application settings outside your code and use Spring Profiles to apply different configurations based on the active environment (e.g., development, testing, production).
* **Concepts:** External configuration (YAML vs. Properties), custom properties, Spring Profiles, `application-{profile}.yml` files, activating profiles via configuration or command line, profile-specific beans.
* **How to Test (conceptual):** Run your application with different active profiles (e.g., using a command-line argument) and observe the loaded configuration properties and activated components in the console.

### [Lab 10: Asynchronous Processing with `@Async`](./Lab10/Lab10.md)

* **Description:** Improve your application's responsiveness by offloading long-running tasks to separate threads using Spring's `@Async` annotation. This allows your main request threads to return quickly to clients.
* **Concepts:** Asynchronous method execution, `@EnableAsync`, `@Async` annotation, thread pools, non-blocking operations, observing thread behavior.
* **How to Test (conceptual):** Make an API call that triggers an asynchronous method. Observe that the API response is immediate, while background processing logs appear later in the console, indicating parallel execution.

### [Lab 11: Containerizing Your Spring Boot Application with Docker](./Lab11/Lab11.md)

* **Description:** Learn the fundamentals of containerizing your Spring Boot application using Docker. You'll build a Docker image for your API and run it inside a portable container, which is a foundational step for modern cloud deployments. This lab includes instructions for installing Docker Desktop.
* **Important Note:** If you are unable to install or run Docker Desktop, you can still follow along with the conceptual steps and observe how containerization works. Your local Spring Boot project will remain functional for subsequent labs.
* **Concepts:** Docker, containers, Docker Desktop, building Docker images with Maven plugin, running Docker containers, port mapping, basic Docker commands (`docker images`, `docker run`, `docker ps`, `docker stop`, `docker rm`).
* **How to Run (conceptual):** After project setup, `mvn clean install spring-boot:build-image`, then `docker run -p 8080:8080 <your-image-name>`.
* **How to Test (conceptual):** Access your API via `http://localhost:8080` (now served from the Docker container) using an API client.

## General Execution Notes

* For most labs, you will typically navigate to your Spring Boot project's root directory (where its `pom.xml` is located) in your terminal.
* To run the application, use the Maven command: `mvn spring-boot:run`.
* To stop the running application, press `Ctrl+C` in the terminal where it's running.
* Ensure necessary dependencies are added to your project's `pom.xml` as instructed in each lab.

## Tools Used

* **Java Development Kit (JDK) 17+**
* **Apache Maven**
* **VS Code** (with relevant Java extensions like "Extension Pack for Java")
* **Thunder Client** (VS Code extension for API testing) or `curl`
* **Web Browser** (for accessing frontend, H2 Console, and Actuator endpoints)
* **Docker Desktop** (for Lab 11)

---
