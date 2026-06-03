# Project Status Review

Review date: 2026-06-03

## 1. Current Git State

- Current branch: `fetch/testing-and-quality-improvements`
- Target remote: `new-origin https://github.com/bluecode963/Clearance-Management-system.git`
- `origin` is still configured for `https://github.com/Yabu920/se4801-Clearance-management-system.git`; do not push there.
- Current testing branch is being prepared from `fetch/registrar-final-approval-workflow`.
- Working tree includes focused backend test additions, one safe forbidden-error handling improvement, and this report update.
- Verification completed on 2026-06-03: backend tests, backend package, and frontend build passed.

Local branches:

```text
fetch/auth-flow-role-guard-and-password-reset
fetch/authentication-jwt-rbac
fetch/backend-config-health-errors
fetch/cicd-pipe/v1
fetch/clearance-request-workflow
fetch/domain-model
fetch/frontend-demo-ui
fetch/office-staff-review-workflow
fetch/persistence-flyway
fetch/project-docs-environment
fetch/registrar-final-approval-workflow
fetch/testing-and-quality-improvements
main
```

Branches visible on `new-origin`:

```text
new-origin/main
new-origin/fetch/auth-flow-role-guard-and-password-reset
new-origin/fetch/authentication-jwt-rbac
new-origin/fetch/backend-config-health-errors
new-origin/fetch/cicd-pipe/v1
new-origin/fetch/clearance-request-workflow
new-origin/fetch/domain-model
new-origin/fetch/frontend-demo-ui
new-origin/fetch/office-staff-review-workflow
new-origin/fetch/persistence-flyway
new-origin/fetch/project-docs-environment
new-origin/fetch/registrar-final-approval-workflow
```

Latest current-branch commits:

```text
Latest commits will be updated after the testing branch commits are created and pushed.
```

## 2. Current Completed Stages

| Stage | Status | Evidence |
|---|---|---|
| Stage 1: Backend foundation | DONE | Spring Boot app, health endpoint, environment-based PostgreSQL config, global exception handler. |
| Stage 2: Domain model and Flyway | DONE | Core entities exist; migrations V1-V10 are present locally, including a development admin seed. |
| Stage 3: Authentication and authorization | DONE | Register/login/logout, JWT generation/validation, blacklist, BCrypt, `/api/auth/me`, Swagger Bearer auth, role-aware login. |
| Stage 4: Secure frontend auth flow | DONE | `App.tsx` uses protected role routes; public navigation tabs removed; login redirects by role; logout clears auth. |
| Stage 5: Admin-only user creation | DONE | `POST /api/admin/users` exists; admin dashboard has create-user form; office staff can receive `officeId`. |
| Stage 6: Forgot/reset password | DONE | Backend endpoints, token table, hashed reset tokens, dev token display, frontend pages exist. |
| Stage 7: Student clearance request workflow | DONE | Student creates one active request, steps auto-created, own list/detail endpoints and frontend integration. |
| Stage 8: Office staff review workflow | DONE | Staff list/view/review assigned office steps; reject comment required; reviewed steps blocked; request status updates. |
| Stage 9: Registrar final approval workflow | DONE | Registrar list/view/decision endpoints and connected frontend dashboard are implemented. |
| Stage 10: Testing and quality improvements | DONE | Focused service tests and security access tests were added for implemented workflows. |

## 3. Current Missing/Partial Stages

| Stage | Status | Notes |
|---|---|---|
| Stage 11: Admin management and final requirements | PARTIAL | Admin can create users, but full admin CRUD/monitoring/search is missing. Docker Compose exists but was not verified in this audit. |

## 4. Implemented Branches and Commits

Important feature branches on `new-origin`:

- `main`: `368aea5 initialize backend spring boot project foundation`
- `fetch/domain-model`: domain entities and enums
- `fetch/persistence-flyway`: repositories and migrations
- `fetch/backend-config-health-errors`: config, health, exceptions
- `fetch/frontend-demo-ui`: first frontend demo UI
- `fetch/project-docs-environment`: docs, environment, Docker Compose
- `fetch/authentication-jwt-rbac`: JWT authentication and initial frontend auth
- `fetch/clearance-request-workflow`: student request creation/tracking
- `fetch/office-staff-review-workflow`: office review workflow
- `fetch/auth-flow-role-guard-and-password-reset`: protected frontend routing, role-aware login, admin user creation, password reset
- `fetch/registrar-final-approval-workflow`: registrar final approval workflow

Auth-flow branch commits:

```text
6c00ba9 add role-aware login validation
423b0bd restrict user creation to admin
8aa20c1 add password reset workflow
bf96c8c secure frontend routes with role guards
068b46b update readme for secure auth flow
```

## 5. Backend Endpoint Summary

### HealthController

- `GET /api/health` - Public

### AuthController

- `POST /api/auth/login` - Public
- `POST /api/auth/forgot-password` - Public
- `POST /api/auth/reset-password` - Public
- `POST /api/auth/register` - ADMIN only by `@PreAuthorize("hasRole('ADMIN')")`
- `POST /api/auth/logout` - Authenticated
- `GET /api/auth/me` - Authenticated

### AdminUserController

- `POST /api/admin/users` - ADMIN only

### ClearanceRequestController

- `POST /api/clearance-requests` - STUDENT only
- `GET /api/clearance-requests/my?page=0&size=10` - STUDENT only
- `GET /api/clearance-requests/{id}` - STUDENT only

### OfficeReviewController

- `GET /api/office/clearance-steps?page=0&size=10&status=PENDING` - OFFICE_STAFF only
- `GET /api/office/clearance-steps/{id}` - OFFICE_STAFF only
- `PATCH /api/office/clearance-steps/{id}/review` - OFFICE_STAFF only

### RegistrarClearanceController

- `GET /api/registrar/clearance-requests?page=0&size=10&status=READY_FOR_REGISTRAR&requestType=GRADUATION&studentId=ATE&keyword=student` - REGISTRAR only
- `GET /api/registrar/clearance-requests/{id}` - REGISTRAR only
- `PATCH /api/registrar/clearance-requests/{id}/decision` - REGISTRAR only

## 6. Frontend Route/Auth Summary

- Public normal flow is login only.
- `ForgotPasswordPage` and `ResetPasswordPage` are public utility pages.
- `RegisterPage.tsx` still exists in the source tree, but it is not imported by `App.tsx` and is not exposed in normal navigation.
- `RegistrarDashboardPage.tsx` is connected to registrar APIs and supports filters, request review, approval, and rejection.
- Protected routes are implemented in `App.tsx` without React Router:
  - `/student` requires `STUDENT`
  - `/admin` requires `ADMIN`
  - `/office-staff` requires `OFFICE_STAFF`
  - `/registrar` requires `REGISTRAR`
- Missing token/user redirects to `/login`.
- Wrong dashboard route redirects to the logged-in user's correct dashboard.
- Logout calls `/api/auth/logout`, clears local storage, and returns to login.
- Token and user are stored in local storage.
- Frontend does not decode JWT expiry. Expired token handling depends on backend 401 responses.

## 7. Database Migration Summary

Migration files currently present:

```text
V1__create_users_table.sql
V2__create_student_profiles_and_offices.sql
V3__create_clearance_requests.sql
V4__create_clearance_steps_and_attachments.sql
V5__create_approval_logs_and_blacklisted_tokens.sql
V6__add_indexes.sql
V7__seed_default_offices.sql
V8__assign_office_to_users.sql
V9__create_password_reset_tokens.sql
V10__seed_default_admin_user.sql
```

Notes:

- Latest migration version present locally: V10.
- V7 seeds default offices: Library, Finance, Department, Dormitory, Registrar.
- V8 adds `users.office_id` for office staff assignment.
- V9 creates `password_reset_tokens`.
- V10 seeds `admin@test.com` with BCrypt-hashed password `admin123` for local development/demo only.
- Seed migrations use safe `WHERE NOT EXISTS`/conflict-safe patterns and should not duplicate rows.

## 8. Build/Test Results

Backend package:

```text
cd backend
mvn clean package -DskipTests
Result: BUILD SUCCESS on 2026-06-03
```

Backend tests:

```text
cd backend
mvn test
Result: BUILD SUCCESS on 2026-06-03
```

Backend test classes now present:

```text
AuthServiceTest
ClearanceRequestServiceTest
OfficeReviewServiceTest
RegistrarClearanceServiceTest
SecurityAccessTest
```

Test result:

```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
```

Coverage focus:

- role-aware authentication login behavior
- student request creation and duplicate active request rejection
- office staff own-office review rules
- registrar final approval and rejection rules
- role-based access rejection for protected API areas

Frontend:

```text
cd frontend
npm install
npm run build
Result: completed successfully on 2026-06-03
```

The shell tool did not print normal npm build details, but both commands exited with code 0.

Docker Compose:

- Docker Compose files exist.
- Docker was not run in this audit.
- Development Compose files now default to `student_clearance_docker_jwt_secret_key_2026`, which satisfies the backend 32+ character JWT secret requirement while still allowing environment override.

## 9. Bugs and Risks

- Registrar final approval has been implemented on `fetch/registrar-final-approval-workflow`.
- `application.properties` may appear modified due to line-ending metadata; content remains environment-variable based with no real secret default.
- `GlobalExceptionHandler` exposes safe `IllegalStateException` messages such as missing JWT secret configuration and does not expose stack traces.
- Focused tests now cover auth, student request workflow, office review, registrar final approval, and security access rules.
- Admin user creation still needs dedicated service/controller tests.
- Admin office CRUD, user list/search, request monitoring, and search/filter with multiple parameters are not complete.
- `RegisterPage.tsx` still exists; not reachable in app, but it may confuse reviewers unless removed or documented.
- Frontend route guarding is client-side only. Backend security is still the real protection, which is correct.
- Local storage token storage is acceptable for this course demo but not ideal for production.
- Docker Compose was not started in this cleanup, so container runtime verification is still pending.
- No attachment upload workflow is implemented.

## 10. Remaining Assignment Requirements

| Requirement | Status |
|---|---|
| Java 21 / Spring Boot 3 / Maven | DONE |
| PostgreSQL + Flyway | DONE |
| Spring Security JWT + BCrypt | DONE |
| Token blacklist/logout | DONE |
| Role-based access with `@PreAuthorize` | DONE for implemented workflows |
| 5+ entities and relationships | DONE |
| 8+ REST endpoints | DONE |
| Pagination on at least 2 endpoints | DONE |
| Bean Validation | DONE |
| Global exception handler | DONE |
| OpenAPI/Swagger | DONE |
| Search/filter endpoint with multiple params | DONE for registrar list filters; broader admin search still missing |
| Docker Compose deployment | PARTIAL, files exist but not currently verified |
| JUnit/Mockito/Security tests | DONE for main implemented workflows; admin user creation and integration tests can still be improved |
| Registrar final approval | DONE |
| Admin CRUD/monitoring | PARTIAL |
| Clear README | PARTIAL, current but should be updated after final workflows |

## 11. Recommended Next Phase

Recommended next phase after this testing branch:

```text
fetch/admin-monitoring-and-final-polish
```

Scope:

- Add admin request monitoring/listing if required for final demo.
- Add dedicated tests for admin user creation.
- Verify Docker Compose on a machine with Docker installed.
- Keep admin CRUD small and demo-focused.

Before starting that phase:

1. Confirm this testing branch is clean and pushed.
2. Manually test the full student to registrar flow in Swagger/frontend.
3. Decide which admin monitoring features are required by the course rubric.

## 12. Exact Commands for Me to Run

Backend local run in Git Bash:

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/student_clearance
export DB_USER=postgres
export DB_PASSWORD="your_postgres_password"
export JWT_SECRET=change_this_to_a_secure_secret_key_32_chars_minimum
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Build checks:

```bash
cd backend
mvn clean package -DskipTests
mvn test

cd ../frontend
npm install
npm run build
```

Check seeded offices:

```sql
SELECT id, office_name FROM offices ORDER BY id;
```

If V10 is applied, login admin:

```text
Role: ADMIN
Email: admin@test.com
Password: admin123
```

## 13. Manual Test Steps in Swagger and Frontend

### Swagger auth

1. Start backend with a 32+ character `JWT_SECRET`.
2. Open `http://localhost:8080/swagger-ui/index.html`.
3. Call `POST /api/auth/login` with email, password, and matching role.
4. Copy token from response.
5. Click Swagger `Authorize`.
6. Paste `Bearer <token>`.
7. Call `GET /api/auth/me`.

### Frontend role login

1. Start frontend with `npm run dev`.
2. Open `http://localhost:5173`.
3. Select the correct role on the login page.
4. Login with valid credentials.
5. Confirm redirect to the matching dashboard.
6. Try the same credentials with the wrong selected role and confirm rejection.

### Admin creates office staff

1. Login as ADMIN.
2. Go to `/admin`.
3. Use Create User form.
4. Role: `OFFICE_STAFF`.
5. Office ID: use value from `SELECT id, office_name FROM offices ORDER BY id`.
6. Submit.
7. Logout and login as that office staff user.

### Student request workflow

1. Login as STUDENT.
2. Create a clearance request.
3. Confirm office steps appear.
4. Try creating a second active request and confirm it is rejected.

### Office review workflow

1. Login as OFFICE_STAFF assigned to an office.
2. Open `/office-staff`.
3. Filter `PENDING`.
4. Approve or reject assigned steps.
5. Confirm reject without comment fails.
6. Confirm already reviewed steps cannot be reviewed again.

### Registrar final approval workflow

1. Approve all non-registrar office steps for a student request.
2. Confirm the request status becomes `READY_FOR_REGISTRAR`.
3. Login as `REGISTRAR`.
4. Open `/registrar`.
5. Confirm the ready request appears.
6. Approve the final clearance and confirm request status becomes `COMPLETED`.
7. For a second ready request, reject with an empty comment and confirm it fails.
8. Reject with a comment and confirm request status becomes `REJECTED`.
