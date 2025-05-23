# Employee Management System

## Description
A comprehensive Spring Boot application for managing employees, departments, and projects. It includes features like CRUD operations, relationship management between entities, input validation, business rule enforcement (e.g., unique email for employees), API security using JWT, and interactive API documentation via Swagger UI.

## Features
*   **Employee Data Management**: Store and manage employee details including name, role, salary, date of birth, unique email, phone number, hire date, and address.
*   **Department Management**: Manage departments within the organization.
*   **Project Management**: Track and manage projects.
*   **Rich Entity Relationships**:
    *   Employee to Department (Many-to-One).
    *   Employee to Manager (Many-to-One, self-referencing for hierarchical structures).
    *   Employee to Project (Many-to-Many).
*   **Comprehensive CRUD APIs**: RESTful APIs for creating, reading, updating, and deleting employees, departments, and projects.
*   **Input Validation**: Bean validation (JSR 303) for request payloads and business rule validation within service layers.
*   **Secure APIs**:
    *   Authentication and authorization using Spring Security and JSON Web Tokens (JWT).
    *   Role-based access control (RBAC) with `ROLE_ADMIN` and `ROLE_USER`.
        *   `ROLE_ADMIN` for all CRUD operations.
        *   `ROLE_USER` for read-only operations (GET requests).
*   **User Management**:
    *   User registration (`/api/v1/auth/register`).
    *   User login (`/api/v1/auth/login`) to obtain JWT.
*   **API Documentation**: Interactive API documentation provided by Springdoc OpenAPI, accessible via Swagger UI.
*   **Database**: In-memory H2 database with a web console for direct database access during development.
*   **Unit Testing**: Comprehensive unit tests for service and controller layers using JUnit 5 and Mockito.

## Technologies Used
*   **Java**: 11
*   **Spring Boot**: 2.7.5
*   **Spring MVC**: For RESTful API development.
*   **Spring Data JPA**: For database interaction and repository layer.
*   **Spring Security**: For authentication and authorization.
*   **Hibernate**: JPA implementation.
*   **H2 Database**: In-memory SQL database.
*   **JWT (jjwt library)**: For JSON Web Token generation and validation.
*   **Lombok**: To reduce boilerplate code (getters, setters, constructors, etc.).
*   **Springdoc OpenAPI**: For generating OpenAPI 3 documentation.
*   **JUnit 5**: For unit testing.
*   **Mockito**: For mocking dependencies in tests.
*   **Maven**: For project build and dependency management.

## Setup and Installation

### Prerequisites
*   JDK 11 or newer installed.
*   Apache Maven installed.

### Steps
1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd employee-management
    ```

2.  **Build the project**:
    ```bash
    mvn clean install
    ```
    *Note: There is a known, persistent compilation issue related to Lombok and the Java compiler (`java.lang.NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport ...`). This issue is likely environment-specific or requires deeper POM configuration adjustments and was not resolved during the last refactoring session.*

3.  **Run the application**:
    *   Using Maven:
        ```bash
        mvn spring-boot:run
        ```
    *   Or by running the main application class `de.zeroco.employeemanagement.EmployeeManagementApplication` from your IDE.

## Accessing the Application
*   **Application Base URL**: `http://localhost:8080`
*   **H2 Database Console**:
    *   URL: `http://localhost:8080/h2-console`
    *   JDBC URL: `jdbc:h2:mem:employeedb`
    *   Username: `sa`
    *   Password: `password`
*   **Swagger UI (API Documentation)**:
    *   URL: `http://localhost:8080/swagger-ui.html`

## Authentication
The application uses JWT-based authentication.

1.  **Register a new user**:
    *   Endpoint: `POST /api/v1/auth/register`
    *   Request Body (example):
        ```json
        {
            "username": "newuser",
            "password": "password123",
            "roles": "ROLE_USER" 
        }
        ```
        (If `roles` is omitted, it defaults to `ROLE_USER`)

2.  **Login to get JWT**:
    *   Endpoint: `POST /api/v1/auth/login`
    *   Request Body (example):
        ```json
        {
            "username": "adminuser",
            "password": "password123"
        }
        ```
    *   Response: Contains the JWT token.

3.  **Accessing Secured Endpoints**:
    *   Include the obtained JWT in the `Authorization` header for requests to secured endpoints.
    *   Format: `Bearer <your_jwt_token>`

### Default Users
Two default users are created on startup by the `DataInitializer` class if they don't already exist:
*   **Admin User**:
    *   Username: `adminuser`
    *   Password: `password123`
    *   Roles: `ROLE_ADMIN, ROLE_USER`
*   **Regular User**:
    *   Username: `regularuser`
    *   Password: `password123`
    *   Roles: `ROLE_USER`

## API Endpoints
The application provides RESTful APIs for managing employees, departments, and projects.

*   **Authentication**: `/api/v1/auth` (for login and registration)
*   **Employees**: `/api/v1/employees`
*   **Departments**: `/api/v1/departments`
*   **Projects**: `/api/v1/projects`

Please refer to the **Swagger UI** at `http://localhost:8080/swagger-ui.html` for detailed information on all available endpoints, request/response schemas, and to try out the APIs.

## Testing
*   **Unit Tests**:
    *   Located in `src/test/java/de/zeroco/employeemanagement`.
    *   Can be executed using Maven:
        ```bash
        mvn test
        ```
*   **Manual API Testing**:
    *   APIs can be tested using tools like Postman or cURL against the running application.
    *   The Swagger UI also provides an interface for interactive API testing.

---
This README provides a comprehensive overview of the Employee Management System.
