# medical-authorizations

Production-style backend API scaffold for a medical authorization service, built with Spring Boot and designed for extensibility, operations, and maintainability.

## Purpose

This project is a **starter foundation** for a real-world backend service responsible for medical authorization workflows.
It focuses on architecture and production readiness rather than implementing business logic.

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- SQL Server (JPA + Flyway)
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
- SQL Server running locally on `localhost:1433`
- Docker (for RabbitMQ and optional API container)

### 1) Local SQL Server setup

Create a SQL login/user with the expected credentials and grant schema permissions in `medical_authorizations`:

```sql
CREATE LOGIN api_user_medauth WITH PASSWORD = 'ApiUserMedAuth123!';
GO

IF DB_ID('medical_authorizations') IS NULL
    CREATE DATABASE medical_authorizations;
GO

USE medical_authorizations;
GO

CREATE USER api_user_medauth FOR LOGIN api_user_medauth;
GO

ALTER ROLE db_datareader ADD MEMBER api_user_medauth;
ALTER ROLE db_datawriter ADD MEMBER api_user_medauth;
ALTER ROLE db_ddladmin ADD MEMBER api_user_medauth;
GO
```

### 2) Run API with Maven (recommended for local development)

```bash
export DB_URL='jdbc:sqlserver://localhost:1433;databaseName=medical_authorizations;encrypt=true;trustServerCertificate=true'
export DB_USERNAME='api_user_medauth'
export DB_PASSWORD='ApiUserMedAuth123!'

mvn spring-boot:run
```

### 3) Run RabbitMQ only in Docker

```bash
docker compose up rabbitmq -d
```

### 4) Run API + RabbitMQ with Docker Compose

When the API runs in Docker, it uses `host.docker.internal` to connect back to your local SQL Server instance.

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
- Liveness/readiness health group configuration (includes DB readiness)
- Security baseline allowing actuator and Swagger in dev-friendly mode

## Persistence and Messaging Readiness

- SQL Server datasource configured through environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- JPA enabled with explicit SQL Server dialect and `ddl-auto=validate`
- Flyway enabled with initial authorization table migration
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
