# Student Clearance Management System

Enterprise Application Development course project for managing university student clearance before graduation, withdrawal, or transfer.

The system will allow a student to submit one clearance request, automatically create required office clearance steps, let office staff approve or reject their assigned steps, and let the registrar make the final decision after all offices approve.

## Group Members

1. Yeabsra Abera
2. Samuel Tesfaye
3. Anteneh Debebe
4. Rekik Alemayehu
5. Yeabsira Eyob

## Backend Tech Stack

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway migrations
- Spring Security 6
- JWT dependencies prepared for later phases
- BCrypt password hashing
- Bean Validation
- Lombok
- SpringDoc OpenAPI / Swagger
- JUnit 5, Mockito, Spring Security Test
- H2 for testing
- JaCoCo

## Frontend Tech Stack

- React
- Vite
- TypeScript
- Tailwind CSS

## Project Structure

```text
se4801-Clearance-management-system/
├── backend/
├── frontend/
├── docker-compose.yml
└── .env.example
```

## Run Backend

1. Copy `.env.example` values into your local environment.
2. Start PostgreSQL:

```bash
docker compose up -d
```

3. Run the backend:

```bash
cd backend
mvn spring-boot:run
```

Swagger UI will be available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Health endpoint:

```text
GET http://localhost:8080/api/health
```

## Run With Docker Compose

Development stack:

```bash
docker compose -f docker-compose.dev.yml up --build
```

The default `docker-compose.yml` also starts the development stack:

```bash
docker compose up --build
```

Development URLs:

```text
Frontend: http://localhost:5173
Backend health: http://localhost:8080/api/health
Swagger: http://localhost:8080/swagger-ui/index.html
PostgreSQL: localhost:5432
```

Production-style stack:

```bash
POSTGRES_PASSWORD=change-me JWT_SECRET=change-me docker compose -f docker-compose.prod.yml up --build
```

Production URL:

```text
Frontend: http://localhost
```

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at:

```text
http://localhost:5173
```

## Authentication API

Phase 2 adds JWT authentication and role-based security.

Public endpoints:

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/health
```

Protected endpoints:

```text
POST /api/auth/logout
GET  /api/auth/me
```

JWT configuration is read from environment variables:

```text
JWT_SECRET
JWT_EXPIRATION_MS
```

Use a `JWT_SECRET` value of at least 32 characters when running login or registration.

## Run Tests

Backend:

```bash
cd backend
mvn test
```

Frontend build check:

```bash
cd frontend
npm run build
```

## Git Workflow Rules

- Work in small, logical commits.
- Do not combine backend domain, frontend UI, README, and configuration work into one large commit.
- Each commit should be easy for all group members to explain during presentation.
- Use clear commit messages that describe the feature or layer changed.
- Do not commit local `.env` files, generated build folders, or dependency folders.

## CI/CD

GitHub Actions workflows are configured for development and production branches:

- `Dev CI/CD` runs on push or pull request to `dev`
- `Prod CI/CD` runs on push or pull request to `main`

Both workflows build the backend, build the frontend, validate the matching Docker Compose file, build Compose images, start the stack, show container status, and shut it down.

## Current Phase Completed

Phase 1 foundation:

- Backend Spring Boot project structure under `backend/`
- Core domain enums and JPA entities
- Repository interfaces
- Flyway migration scripts
- Basic OpenAPI and Spring Security configuration
- Health endpoint
- Global exception handling foundation
- React + Vite + TypeScript + Tailwind frontend under `frontend/`
- Simple role-based page placeholders
- Docker Compose PostgreSQL service
