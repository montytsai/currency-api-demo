# 📦 Project Currency API Demo

[![Java CI with Maven](https://github.com/montytsai/currency-api-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/montytsai/currency-api-demo/actions/workflows/ci.yml)

A showcase project demonstrating robust backend engineering practices with Java 8 and Spring Boot 2. 
The core focus is to illustrate how a well-structured, fully-tested, and CI-automated service is built, using the Coindesk API as a case study for third-party API integration and data transformation.

English | [繁體中文](README.zh-TW.md)

---

## 📄 Interactive API Documentation (Swagger UI)

After starting the application, the interactive API documentation is available at:

> **Swagger UI**: [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)

---

## ✨ Features

- **Modular & Layered Architecture**: Features are isolated into modules with a clear, layered internal structure, promoting maintainability and scalability.
- **Database Operations (CRUD)**: Full implementation of Create, Read, Update, and Delete operations using Spring Data JPA.
- **Third-Party API Integration**: Demonstrates consuming, processing, and transforming data from an external REST API. 
- **Data Transformation Layer**: Enriches raw external data into a custom internal format using a dedicated Mapper layer. 
- **Unit & Integration Testing**: Test coverage for key business logic and API endpoints using JUnit 5 and Mockito. 
- **API Documentation**: Auto-generated, interactive API documentation via Swagger (SpringDoc).
- **CI Automation**: A GitHub Actions workflow automatically builds and tests the project on every push to ensure code stability.

---

## 🛠️ Tech Stack

- **Backend**: Java 8, Spring Boot 2.7.18
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Docs**: SpringDoc OpenAPI (Swagger UI)
- **Automation**: Maven, GitHub Actions

---

## 🏛️ Project Structure & Modularization

This project follows a "**Package by Feature, Layered Inside**" structure to achieve high cohesion and low coupling.
> This modularization enables a clear separation of concerns, ease of testing, and scalability for future feature additions.

```
io.github.montytsai.currencyapi
│
├── currency         # Currency Management Module
│ ├── controller     # REST Endpoints
│ ├── service        # Business Logic (Interface & Impl)
│ ├── repository     # Data Access (JPA)
│ ├── entity         # JPA Entities
│ └── dto            # Data Transfer Objects
│
├── coindesk         # External API Integration Module
│ ├── controller
│ ├── service
│ ├── mapper         # Data Transformation Logic
│ └── dto
│
├── config           # Global Configuration (e.g., Swagger)
└── exception        # Global Exception Handling
```

---

## 🚀 Getting Started

### Prerequisites

- Java 8
- Maven

### Run Locally

```bash
mvn spring-boot:run
```

App will be available at: http://localhost:8080

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📦 Docker Support
(The project is container-ready with a provided Dockerfile)

```bash
# Build the Docker image
docker build -t currency-api-demo .

# Run the container
docker run -p 8080:8080 currency-api-demo
```

---

## 📝 API Reference

See [Swagger UI](#-interactive-api-documentation-swagger-ui) for complete endpoint details.

---

## 📄 License
This project is for demonstration and educational purposes.