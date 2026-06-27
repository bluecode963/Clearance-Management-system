# Final Submission Readiness Report

## 1. Current Branch and Remote

- Current branch: `fetch/student-correction-resubmission-workflow`
- Current HEAD: `9b15378 show student attachments to assigned offices`
- Remote to use: `new-origin`
- `new-origin` URL: `https://github.com/bluecode963/Clearance-Management-system.git`
- `origin` still exists and points to `https://Yabu920@github.com/Yabu920/se4801-Clearance-management-system.git`
- Do not push to `origin`.
- Working tree during audit: clean.
- Current branch status: local HEAD matches `new-origin/fetch/student-correction-resubmission-workflow`.

Local feature branches found:

- `fetch/auth-flow-role-guard-and-password-reset`
- `fetch/authentication-jwt-rbac`
- `fetch/backend-config-health-errors`
- `fetch/cicd-pipe/v1`
- `fetch/clearance-request-workflow`
- `fetch/domain-model`
- `fetch/frontend-demo-ui`
- `fetch/office-staff-review-workflow`
- `fetch/persistence-flyway`
- `fetch/project-docs-environment`
- `fetch/registrar-final-approval-workflow`
- `fetch/role-activities-and-workflow-polish`
- `fetch/student-correction-resubmission-workflow`
- `fetch/testing-and-quality-improvements`
- `main`

Important pushed branches on `new-origin`:

- `new-origin/main`
- `new-origin/fetch/domain-model`
- `new-origin/fetch/persistence-flyway`
- `new-origin/fetch/backend-config-health-errors`
- `new-origin/fetch/frontend-demo-ui`
- `new-origin/fetch/project-docs-environment`
- `new-origin/fetch/authentication-jwt-rbac`
- `new-origin/fetch/clearance-request-workflow`
- `new-origin/fetch/office-staff-review-workflow`
- `new-origin/fetch/auth-flow-role-guard-and-password-reset`
- `new-origin/fetch/registrar-final-approval-workflow`
- `new-origin/fetch/role-activities-and-workflow-polish`
- `new-origin/fetch/student-correction-resubmission-workflow`
- `new-origin/fetch/testing-and-quality-improvements`
- `new-origin/fetch/cicd-pipe/v1`

## 2. Completed Features

### Backend Foundation: DONE

- Spring Boot application exists under `backend/`.
- Package structure uses `com.se4801.clearance`.
- Health endpoint exists: `GET /api/health`.
- PostgreSQL connection is configured through environment variables.
- Flyway is enabled.
- JPA uses `spring.jpa.hibernate.ddl-auto=validate`.
- Swagger/OpenAPI is configured with Bearer JWT authorization.
- Global exception handler returns clean JSON errors.

### Domain Model and Database: DONE

Implemented entities:

- `User`
- `StudentProfile`
- `Office`
- `ClearanceRequest`
- `ClearanceStep`
- `Attachment`
- `ApprovalLog`
- `BlacklistedToken`
- `PasswordResetToken`

Implemented enums:

- `Role`
- `ClearanceType`
- `ClearanceRequestStatus`
- `ClearanceStepStatus`
- `AttachmentKind`
- `AttachmentPurpose`

Flyway migrations V1 through V12 exist and cover schema creation, indexes, default offices, office assignment, password reset tokens, default admin seed, correction/resubmission statuses, and workflow attachments.

### Authentication and Authorization: DONE

- Role-aware login exists.
- Login requires selected role and rejects wrong role.
- JWT generation and validation exist.
- JWT secret is configurable and has a safe development fallback.
- BCrypt password hashing is used.
- Logout blacklists tokens.
- `GET /api/auth/me` works for authenticated users.
- Public self-registration is no longer open to normal users.
- `POST /api/auth/register` is admin protected.
- Admin-only user creation exists at `POST /api/admin/users`.
- Forgot/reset password flow exists.

### Secure Frontend Auth Flow: DONE

- App opens to login.
- Public dashboard tabs were removed.
- Login and forgot/reset password are public.
- Dashboard pages are protected by token and role.
- Wrong-role access redirects to the correct role dashboard.
- Logout clears stored auth and redirects to login.

### Admin Workflow: DONE for course demo, PARTIAL for full production CRUD

- Admin can log in with seeded local account.
- Admin dashboard has system overview.
- Admin can create students, office staff, registrars, and admins.
- Office staff can be assigned to an office at creation time.
- Admin can list active offices for the create-user form.
- Full user edit/delete and office CRUD are not implemented.

### Student Workflow: DONE

- Student can create one active clearance request.
- Duplicate active request is rejected.
- System automatically creates office steps.
- Student can list own requests.
- Student can view request status, office progress, comments, and final status.
- Student can resubmit a correction to the specific office step.
- Student cannot access other students' request endpoints through the service checks.

### Office Staff Workflow: DONE

- Office staff can list only steps for their assigned office.
- Office staff can filter by step status.
- Office staff can view one assigned step.
- Office staff can approve pending or resubmitted steps.
- Office staff can reject with required comment.
- Rejection creates `NEEDS_CORRECTION`, not final rejection.
- Office staff cannot review registrar steps.
- Office staff cannot review another office's step.
- Office staff cannot review already approved/rejected steps.

### Registrar Workflow: DONE

- Registrar can list requests ready for registrar review.
- Registrar endpoint supports filters by status, request type, student ID, keyword, page, and size.
- Registrar can view ready/completed/rejected requests.
- Registrar can approve only `READY_FOR_REGISTRAR` requests.
- Registrar can reject with required comment.
- Registrar approval requires an attachment.
- Registrar rejection attachment is optional.
- Student sees final status through their request view.

### Student Correction and Resubmission Workflow: DONE

- Office rejection sets step to `NEEDS_CORRECTION`.
- Request becomes `NEEDS_CORRECTION`.
- Student can resubmit only correction/rejected office steps.
- Approved steps remain approved.
- Resubmitted step goes back only to the same office.
- Office staff can review `RESUBMITTED` steps.
- Registrar cannot approve until all non-registrar office steps are approved.

### Attachments: DONE

- One upload input supports document or picture files.
- Student resubmission attachment is optional.
- Office review attachment is optional.
- Registrar approval attachment is required.
- Registrar rejection attachment is optional.
- Assigned office staff can view request attachments uploaded by the student.
- Attachments are stored outside Git in `backend/uploads/` by default.

## 3. Partial Features

- Automated tests are PARTIAL/MISSING on the current branch. A separate `fetch/testing-and-quality-improvements` branch exists with test commits, but the current submission branch has no tracked `backend/src/test` files.
- Docker Compose is configured, but this audit did not start Docker because the local environment previously did not have Docker available.
- CI/CD workflow files exist for `dev` and `main`, but they were not executed during this local audit.
- Full admin CRUD is not implemented; only create user, active offices list, and overview exist.
- Frontend routing is implemented manually with browser history, not React Router. This is acceptable for a simple demo but less robust than a production routing library.

## 4. Missing Features

- Real test classes are missing from the current branch.
- Full admin edit/delete user management is missing.
- Office CRUD is missing beyond listing active offices.
- Email sending for password reset is missing. The current reset flow is development-friendly and returns the reset token only in the `dev` profile.
- Advanced reporting/export is missing.
- Production file storage is local-volume based, not cloud/object storage.

## 5. Assignment Checklist

| Requirement | Status | Notes |
|---|---|---|
| Java 21 | DONE | `pom.xml` uses Java 21. |
| Spring Boot 3.x | DONE | Spring Boot 3.3.5. |
| Maven | DONE | Backend builds with Maven. |
| Spring Data JPA | DONE | Repositories and JPA entities exist. |
| PostgreSQL 15+ | DONE | PostgreSQL configured; Compose uses Postgres 16. |
| Flyway V1-V5 minimum | DONE | V1-V12 exist. |
| Spring Security 6 | DONE | Security filter chain and method security exist. |
| JWT login/register/logout | DONE | Login/logout implemented; register is admin-protected. |
| BCrypt password hashing | DONE | PasswordEncoder bean and BCrypt used. |
| Token blacklist/logout | DONE | `BlacklistedToken` and service exist. |
| At least 3 roles | DONE | ADMIN, STUDENT, OFFICE_STAFF, REGISTRAR. |
| `@PreAuthorize` usage | DONE | Used on role APIs. |
| 5+ JPA entities with relationships | DONE | More than 5 entities with relationships. |
| 8+ REST endpoints | DONE | More than 8 endpoints implemented. |
| Pagination on at least 2 list endpoints | DONE | Student, office, registrar list endpoints use pagination. |
| Bean Validation | DONE | Request DTOs use validation annotations. |
| Global exception handler | DONE | Clean API errors. |
| Search/filter endpoint with multiple params | DONE | Registrar search supports multiple filters. |
| Docker Compose deployment | DONE | Dev/prod Compose files exist. |
| OpenAPI/Swagger documentation | DONE | Swagger UI and Bearer auth exist. |
| JUnit/Mockito/Testcontainers tests | MISSING on current branch | No test classes in current branch. |
| Clear README | DONE | README has setup, roles, workflow, and commands. |
| Meaningful Git history | DONE | Multiple logical feature branches and commits exist. |

## 6. API Endpoint List

### Health

- `GET /api/health` - Public, returns DTO.

### Auth

- `POST /api/auth/login` - Public, validates login DTO and role.
- `POST /api/auth/logout` - Authenticated, validates logout DTO.
- `POST /api/auth/forgot-password` - Public, validates email DTO.
- `POST /api/auth/reset-password` - Public, validates reset DTO.
- `GET /api/auth/me` - Authenticated, returns `UserResponse`.
- `POST /api/auth/register` - ADMIN only, validates register DTO.

### Admin

- `POST /api/admin/users` - ADMIN only, validates admin create-user DTO, returns `UserResponse`.
- `GET /api/admin/offices` - ADMIN only, returns office DTOs.
- `GET /api/admin/overview` - ADMIN only, returns overview DTO.

### Student Clearance Requests

- `POST /api/clearance-requests` - STUDENT only, validates request DTO, returns DTO.
- `GET /api/clearance-requests/my?page=0&size=10` - STUDENT only, paginated DTO response.
- `GET /api/clearance-requests/{id}` - STUDENT only, ownership checked in service.
- `PATCH /api/clearance-requests/steps/{stepId}/resubmit` - STUDENT only, JSON or multipart.

### Office Staff

- `GET /api/office/clearance-steps?page=0&size=10&status=PENDING` - OFFICE_STAFF only, paginated DTO response.
- `GET /api/office/clearance-steps/{id}` - OFFICE_STAFF only, assigned office checked.
- `PATCH /api/office/clearance-steps/{id}/review` - OFFICE_STAFF only, JSON or multipart.

### Registrar

- `GET /api/registrar/clearance-requests` - REGISTRAR only, paginated and filterable.
- `GET /api/registrar/clearance-requests/{id}` - REGISTRAR only.
- `PATCH /api/registrar/clearance-requests/{id}/decision` - REGISTRAR only, JSON or multipart.

### Attachments

- `GET /api/attachments/{id}` - Authenticated. Access rules are enforced in service.

## 7. Role-Based Workflow Summary

Recommended demo flow:

1. Admin logs in.
2. Admin creates one student.
3. Admin creates office staff for Library, Finance, Department, and Dormitory.
4. Admin creates one registrar.
5. Student logs in and creates a clearance request.
6. Each office staff user logs in and reviews only their own office step.
7. If one office requests correction, student resubmits that specific step.
8. The same office reviews the resubmitted step.
9. When all non-registrar office steps are approved, request becomes `READY_FOR_REGISTRAR`.
10. Registrar logs in and gives final approval with required attachment.
11. Student logs in and sees `COMPLETED`.

## 8. Database Migrations Summary

- `V1__create_users_table.sql` - creates users table.
- `V2__create_student_profiles_and_offices.sql` - creates student profiles and offices.
- `V3__create_clearance_requests.sql` - creates clearance requests.
- `V4__create_clearance_steps_and_attachments.sql` - creates clearance steps and initial attachments.
- `V5__create_approval_logs_and_blacklisted_tokens.sql` - creates approval logs and blacklisted tokens.
- `V6__add_indexes.sql` - adds useful indexes.
- `V7__seed_default_offices.sql` - seeds Library, Finance, Department, Dormitory, Registrar safely with conflict handling.
- `V8__assign_office_to_users.sql` - adds office assignment to users for office staff.
- `V9__create_password_reset_tokens.sql` - creates password reset tokens.
- `V10__seed_default_admin_user.sql` - seeds `admin@test.com` with BCrypt password `admin123` for local/demo only.
- `V11__update_correction_resubmission_status_constraints.sql` - updates request/step status checks for correction workflow.
- `V12__extend_attachments_for_workflow_evidence.sql` - extends attachments for workflow evidence and indexes.

Migration order is safe for a fresh database. Default admin and default offices are development/demo data, not production data.

## 9. Build and Test Results

Commands run during this audit:

```bash
cd backend
mvn clean package -DskipTests
mvn test

cd ../frontend
npm install
npm run build
```

Results:

- Backend build: PASS.
- Backend tests: PASS command, but no tests were found or run.
- Real test classes exist in current branch: no.
- Frontend install: PASS.
- Frontend build: PASS.
- Blocking build errors: none found.
- Non-blocking warning: Maven shows annotation-processing informational warning because Lombok annotation processing is detected automatically.

## 10. Manual Test Plan

### A. Admin Test

1. Start backend and frontend.
2. Login as `ADMIN`:
   - Email: `admin@test.com`
   - Password: `admin123`
3. Create a student user.
4. Create office staff users and assign offices:
   - Library
   - Finance
   - Department
   - Dormitory
5. Create registrar user.
6. Confirm admin overview counts update.

### B. Student Test

1. Login as the student.
2. Create clearance request with type `GRADUATION`.
3. Confirm office steps are created.
4. Try creating another active request.
5. Expected result: duplicate active request is rejected.

### C. Office Staff Test

1. Login as Library staff.
2. Confirm only Library steps are visible.
3. Approve or reject Library step.
4. Login as Finance staff.
5. Confirm Finance sees only Finance steps.
6. Confirm staff cannot review another office step by changing URL/API ID.

### D. Correction/Resubmission Test

1. Office staff rejects one step with a comment.
2. Student logs in and sees correction reason.
3. Student resubmits only that step, optionally with one document or picture.
4. Same office staff logs in and sees the request attachment.
5. Same office approves the resubmitted step.
6. Confirm previously approved offices remain approved.

### E. Registrar Test

1. Approve all non-registrar office steps.
2. Confirm request becomes `READY_FOR_REGISTRAR`.
3. Login as registrar.
4. Open ready requests.
5. Approve final clearance with required attachment.
6. Student logs in and sees `COMPLETED`.

### F. Negative Security Tests

1. Login as student and try `/admin`.
2. Expected: redirected or access denied.
3. Login as office staff and try `/registrar`.
4. Expected: redirected or access denied.
5. Login as registrar and try admin create user API.
6. Expected: forbidden.
7. Try wrong-role login.
8. Expected: `Selected role does not match this account`.

## 11. Must Fix Today

These affect grading or demo confidence:

- MISSING real tests on the current branch. If time allows, merge or recreate a small set of tests from `fetch/testing-and-quality-improvements`; otherwise be honest that manual testing was used.
- Perform a full manual smoke test with real users before submission.
- Confirm the branch you submit is `fetch/student-correction-resubmission-workflow` or a branch that contains it.
- Confirm Flyway V1-V12 applies on a fresh or clean database before demo.
- Confirm frontend points to the backend URL: `VITE_API_BASE_URL=http://localhost:8080` for local Vite dev, or `/api` for Docker/Nginx.
- Confirm Docker is installed if you plan to demo with Docker Compose.

## 12. Optional Improvements

Do not do these if time is short:

- Full admin user edit/delete.
- Full office CRUD.
- Pretty UI redesign.
- Email integration for password reset.
- Cloud file storage.
- Advanced reports/export.
- Large refactor to React Router.
- Complex test suite if it risks breaking the stable branch.

## 13. Risks That Can Affect Grading

- Current branch has no real test classes, even though dependencies and JaCoCo are configured.
- Docker Compose was not started during this audit, so final Docker runtime should be checked manually if Docker is available.
- Password reset is local/development style; it does not send emails.
- Default admin seed is useful for demo but should be described as local/demo only.
- Attachments are stored on local disk/volume; if the upload directory is deleted, files are gone.
- `origin` still exists. Be careful to push only to `new-origin`.

## 14. Submission Recommendation

Readiness: ALMOST ready.

The core application features required for a strong university demo are implemented and builds pass. The main weakness is lack of real tests on the current branch. If the teacher checks test coverage strictly, add or merge a small focused test set before final submission. If tomorrow's grading is mostly demo plus code review, the project is in good shape after manual smoke testing.

Suggested final readiness score: 8/10.

## 15. Commands for Demo

Backend local:

```bash
cd backend
mvn spring-boot:run
```

Frontend local:

```bash
cd frontend
npm run dev
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

Frontend:

```text
http://localhost:5173
```

Docker development, if Docker is installed:

```bash
docker compose up --build
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

## 16. Recommended Next Action

1. Run the manual smoke test plan above.
2. If everything passes, submit the current branch or merge it into the required submission branch.
3. If there is still time, add a few high-value tests for auth, student request creation, office review, and registrar approval.
