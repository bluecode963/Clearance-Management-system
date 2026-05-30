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

## Authentication and User Management

The current authentication flow uses JWT, BCrypt password hashing, token blacklist logout, and role-based access control.

Public endpoints:

```text
POST /api/auth/login
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/health
```

Protected endpoints:

```text
POST /api/auth/register
POST /api/auth/logout
GET  /api/auth/me
POST /api/admin/users
```

Public self-registration is not part of the normal user flow. Users log in with a selected role, and the backend rejects login if the selected role does not match the account. Admin users create new student, office staff, registrar, or admin accounts from the protected admin dashboard or with `POST /api/admin/users`.

Example login body:

```json
{
  "email": "student1@test.com",
  "password": "password123",
  "role": "STUDENT"
}
```

Example admin user creation body:

```json
{
  "fullName": "Library Staff",
  "email": "library.staff@test.com",
  "password": "password123",
  "role": "OFFICE_STAFF",
  "officeId": 1
}
```

Forgot password is development-friendly. It always returns a generic message and returns the reset token only when the backend runs with the `dev` profile.

Student clearance request endpoints:

```text
POST /api/clearance-requests
GET  /api/clearance-requests/my?page=0&size=10
GET  /api/clearance-requests/{id}
```

Only users with the `STUDENT` role can create and view their own clearance requests. When a student creates a request, the backend automatically creates clearance steps for active offices seeded by Flyway.

Office staff review endpoints:

```text
GET   /api/office/clearance-steps?page=0&size=10&status=PENDING
GET   /api/office/clearance-steps/{id}
PATCH /api/office/clearance-steps/{id}/review
```

Only users with the `OFFICE_STAFF` role can access these endpoints. Office staff users must be assigned to an office before they can review steps.

For local testing, register an office staff user and assign the user to an office with SQL:

```sql
UPDATE users
SET office_id = (SELECT id FROM offices WHERE office_name = 'Library')
WHERE email = 'library.staff@test.com';
```

Example review body:

```json
{
  "decision": "APPROVED",
  "comment": "Cleared by library"
}
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

Completed phases:

- Backend Spring Boot project structure under `backend/`
- Core domain enums and JPA entities
- Repository interfaces
- Flyway migration scripts
- Basic OpenAPI and Spring Security configuration
- Health endpoint
- Global exception handling foundation
- React + Vite + TypeScript + Tailwind frontend under `frontend/`
- Docker Compose PostgreSQL service
- JWT authentication with role-aware login
- Protected frontend dashboard routing
- Admin-only user creation
- Development password reset flow
- Student clearance request creation and tracking
- Office staff clearance step review workflow
