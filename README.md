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
POSTGRES_PASSWORD=change-me JWT_SECRET=student_clearance_docker_jwt_secret_key_2026 docker compose -f docker-compose.prod.yml up --build
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

Local development admin seed:

```text
Email: admin@test.com
Password: admin123
Role: ADMIN
```

This account is for local development/demo only and is created by Flyway with a BCrypt-hashed password.

Student clearance request endpoints:

```text
POST /api/clearance-requests
GET  /api/clearance-requests/my?page=0&size=10
GET  /api/clearance-requests/{id}
PATCH /api/clearance-requests/steps/{stepId}/resubmit
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

Registrar final approval endpoints:

```text
GET   /api/registrar/clearance-requests?page=0&size=10&status=READY_FOR_REGISTRAR
GET   /api/registrar/clearance-requests/{id}
PATCH /api/registrar/clearance-requests/{id}/decision
```

Only users with the `REGISTRAR` role can access these endpoints. The registrar can finalize only requests with status `READY_FOR_REGISTRAR`, after all non-registrar office steps are approved.

Example final approval body:

```json
{
  "decision": "APPROVED",
  "comment": "Final clearance approved"
}
```

Example final rejection body:

```json
{
  "decision": "REJECTED",
  "comment": "Registrar found missing final requirement"
}
```

JWT configuration is read from environment variables:

```text
JWT_SECRET
JWT_EXPIRATION_MS
```

Direct Maven runs use the `dev` profile by default and fall back to the local-only secret
`student_clearance_local_dev_jwt_secret_key_2026`. Docker Compose development uses
`student_clearance_docker_jwt_secret_key_2026`. Either value can be overridden with the
`JWT_SECRET` environment variable.

The `prod` profile has no fallback. Production must provide a `JWT_SECRET` value of at least
32 characters.

Workflow actions support one attachment that can be a document (`PDF`, `DOC`, `DOCX`, or `TXT`)
or picture (`JPG`, `PNG`, or `WEBP`), with a 10 MB limit. Student correction and office review
attachments are optional. Registrar approval requires one attachment; registrar rejection
attachments are optional. Local files are stored under `backend/uploads/`
by default and the directory can be changed with `FILE_UPLOAD_DIR`.

Final bug fixes before submission:

- Student resubmission attachments are linked to the clearance request and step, so the assigned office staff can view and download them.
- Office review attachments are linked to the same request and step, so the student can view and download staff evidence or comments.
- Attachment responses include safe metadata only: file name, content type, size, purpose, uploader name/role, upload time, and a protected download URL.
- Registrar dashboard empty state now clearly says `No requests ready for registrar review.` when no ready requests exist.
- Frontend API errors now show the backend safe error message or HTTP status instead of hiding failures behind a generic message.
- Registrar dashboard real loading error was fixed by typing nullable registrar search filters correctly in the backend query. `GET /api/registrar/clearance-requests?status=READY_FOR_REGISTRAR&page=0&size=10` now returns 200 and lists ready requests.

## Role-Based Activities and Workflow

### Admin

Admins can login as `ADMIN`, create users, create student accounts, create office staff accounts, create registrar accounts, create admin accounts, assign office staff users to offices, view role/activity guidance, and view basic system overview totals. Admins must not create student clearance requests, perform office staff approval, or make registrar final decisions.

### Student

Students can login as `STUDENT`, create one active clearance request, choose `GRADUATION`, `WITHDRAWAL`, or `TRANSFER`, view their own requests, view request details, track office step progress, view comments and rejection reasons, and view final status. Students must not access admin, office staff, or registrar activities.

### Office Staff

Office staff can login as `OFFICE_STAFF`, view only clearance steps assigned to their own office, filter by status, approve assigned pending steps, and reject assigned pending steps with a required comment. Office staff cannot review other offices' steps, review already approved or rejected steps, create users, create student requests, or make registrar final decisions.

### Registrar

Registrars can login as `REGISTRAR`, view requests ready for registrar review, filter requests by status, request type, student ID, and keyword, view request details and office steps, final approve ready requests, and final reject ready requests with a required comment. Registrars cannot approve in-review requests, approve requests with pending or rejected office steps, decide already completed or rejected requests, create users, or perform office step review.

### Full Clearance Workflow

```text
Admin creates users
-> Student logs in
-> Student creates clearance request
-> System creates office steps
-> Office staff review assigned steps
-> If any office requests correction, request becomes NEEDS_CORRECTION
-> If all required offices approve, request becomes READY_FOR_REGISTRAR
-> Registrar reviews request
-> Registrar approves or rejects final clearance
-> If approved, request becomes COMPLETED
-> Student views final status
```

### Student Correction / Resubmission Workflow

```text
Office staff rejects one office step
-> Request becomes NEEDS_CORRECTION
-> Student views rejection reason
-> Student fixes only that office issue
-> Student requests re-review only for that office step
-> That step becomes RESUBMITTED
-> Only the same office reviews again
-> If approved, clearance continues
-> If all offices approve, request becomes READY_FOR_REGISTRAR
-> Registrar gives final decision
```

Approved offices stay approved and do not review again. Office staff rejection means a correction is needed, not a final clearance rejection. Final `REJECTED` status is reserved for registrar final rejection.

### Permission Matrix

| Activity | Admin | Student | Office Staff | Registrar |
|---|---|---|---|---|
| Login | Yes | Yes | Yes | Yes |
| Create users | Yes | No | No | No |
| Create clearance request | No | Yes | No | No |
| View own request | No | Yes | No | No |
| Review assigned office step | No | No | Yes | No |
| Request re-review for corrected step | No | Yes | No | No |
| Review resubmitted office step | No | No | Yes | No |
| Final approve/reject clearance | No | No | No | Yes |
| Reset password | Yes | Yes | Yes | Yes |

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
- Registrar final approval workflow
- Role-based dashboard activity guidance and admin overview
