# medical-authorizations

Production-style backend API scaffold for a medical authorization service, built with Spring Boot and designed for extensibility, operations, and maintainability.

## Purpose

This project is a **starter foundation** for a real-world backend service responsible for medical authorization workflows.
It focuses on architecture and production readiness rather than implementing business logic.

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- SQL Server (JPA + Flyway ready)
- RabbitMQ (integration-ready placeholders)
- Spring Boot Actuator
- Micrometer + Prometheus
- Springdoc OpenAPI (Swagger UI)
- Docker / Docker Compose
- Kubernetes-ready folder structure (`infra/k8s`)

## Architecture Overview

The codebase is organized to support layered design and evolution toward Ports & Adapters:

- `domain`: core business model(s)
- `application`: use-case ports/services (application orchestration)
- `api`: controllers, transport DTOs, and API error mapping
- `infrastructure`: persistence/messaging/configuration adapters
- `configuration`: cross-cutting framework configuration (security, OpenAPI)
- `shared`: reusable technical concerns (logging, web filters)

## Project Structure

```text
src/main/java/com/damian/medicalauthorization
├── api
│   ├── controller
│   ├── dto
│   └── error
├── application
│   ├── port
│   └── service
├── configuration
├── domain
│   └── model
├── infrastructure
│   ├── config
│   ├── messaging
│   └── persistence
│       ├── entity
│       └── repository
└── shared
    ├── logging
    └── web
```

## Run Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker (optional for local stack)

### 1) Run with Maven

```bash
export DB_URL='jdbc:sqlserver://localhost:1433;databaseName=medical_authorizations;encrypt=true;trustServerCertificate=true'
export DB_USERNAME='sa'
export DB_PASSWORD='YourStrong!Passw0rd'

mvn spring-boot:run
```

### 2) Run with Docker Compose

```bash
docker compose up --build
```

## Required Environment Variables

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Optional:

- `SERVER_PORT` (default `8080`)
- `SPRING_PROFILES_ACTIVE` (e.g., `dev`)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- `RABBITMQ_EXCHANGE`, `RABBITMQ_AUTH_CREATED_KEY`

## API and Ops Endpoints

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Included API Stubs

- `POST /api/v1/authorizations`
- `GET /api/v1/authorizations/{id}`

Both endpoints are intentionally minimal and suitable as extension points.

## Operational Readiness Included

- Correlation ID filter (`X-Correlation-Id`) with MDC propagation
- Request logging filter with method/path/status/duration
- Consistent error response model via `@ControllerAdvice`
- Validation via Jakarta Bean Validation
- Actuator + Prometheus exposure
- Liveness/readiness health group configuration
- Security baseline allowing actuator and Swagger in dev-friendly mode

## Persistence and Messaging Readiness

- SQL Server datasource configured through environment variables only
- JPA enabled with explicit SQL Server dialect
- Flyway configured with initial migration placeholder
- RabbitMQ properties and configuration placeholders for future async workflows

## Testing

Run tests with:

```bash
mvn test
```

Included tests:

- Application context load test
- Controller smoke tests for create/get authorization stubs

## Future Evolution Notes

Recommended next implementation increments:

1. Replace stub service with application services + domain rules.
2. Add persistence adapter (JPA entities, repositories, and mappers).
3. Add real authentication/authorization strategy (OAuth2/JWT).
4. Add event publishing for authorization lifecycle changes.
5. Add Kubernetes manifests and deployment overlays.
6. Add integration tests with Testcontainers (SQL Server/RabbitMQ).
