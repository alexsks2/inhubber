# Backend — Java Spring Microservices

## How to Run

### Prerequisites

- Java 17+
- Maven
- Docker

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This starts a PostgreSQL container on port `5432` with:

| Setting  | Value      |
|----------|------------|
| Host     | localhost  |
| Port     | 5432       |
| Database | postgres   |
| Username | inhubber   |
| Password | inhubber   |

### 2. Start Identity Service

```bash
cd identity-service
mvn spring-boot:run
```

or start from IDE. Runs on **http://localhost:8081**

On startup, an admin user is automatically created:

| Field    | Value            |
|----------|------------------|
| Email    | admin@acme.com   |
| Password | admin123         |
| Role     | ADMIN            |

### 3. Start Main Service

```bash
cd main-service
mvn spring-boot:run
```

or start from IDE. Runs on **http://localhost:8080**

### 4. Run Tests

```bash
# From the root directory
mvn test
```

---

## Architecture

```
                        ┌─────────────────────┐
                        │   identity-service   │
  POST /auth/login  ──► │      :8081           │
  POST /auth/register   │                      │
  GET  /auth/validate   │  users table (PG)    │
                        └─────────┬────────────┘
                                  │ GET /auth/validate
                                  │ (Bearer token)
                        ┌─────────▼────────────┐
  GET/POST/PATCH    ──► │    main-service       │
  /companies            │      :8080            │
  /persons              │                       │
                        │  companies table (PG) │
                        │  persons table   (PG) │
                        └───────────────────────┘
```

### Services

**identity-service** — handles authentication only:
- Stores users (`email`, `password` as BCrypt hash, `role`)
- Issues JWT tokens on login (24h expiry)
- Exposes `GET /auth/validate` for internal token validation

**main-service** — handles business logic:
- On every request, calls `GET /auth/validate` on identity-service to verify the Bearer token and resolve the caller's email and role
- Manages `Company` and `Person` entities with permission enforcement

### Authentication Flow

```
1. Client → POST /auth/login → identity-service
2. identity-service → returns JWT token
3. Client → request with "Authorization: Bearer <token>" → main-service
4. main-service → GET /auth/validate → identity-service
5. identity-service → returns { email, role }
6. main-service → enforces permissions → returns response
```

### Email Synchronisation

When an admin updates a person's email via `PATCH /persons/{id}/email`, main-service also calls `PATCH /auth/users/email` on identity-service to keep the `users` table in sync. If the user has not registered yet (404), the sync is skipped and only `persons` is updated.

---

## API Endpoints

### Identity Service — `http://localhost:8081`

| Method | Path                  | Auth     | Description                        |
|--------|-----------------------|----------|------------------------------------|
| POST   | /auth/login           | No       | Login, returns JWT token           |
| POST   | /auth/register        | No       | Register new user (role: USER)     |
| GET    | /auth/validate        | Bearer   | Validate token, returns email+role |
| PATCH  | /auth/users/email     | Internal | Sync email update from main-service |

### Main Service — `http://localhost:8080`

| Method | Path                  | Role        | Description                              |
|--------|-----------------------|-------------|------------------------------------------|
| POST   | /companies            | ADMIN       | Create a company                         |
| GET    | /companies/{id}       | ADMIN, USER | Get company by ID                        |
| POST   | /persons              | ADMIN       | Create a person                          |
| GET    | /persons              | ADMIN       | List all persons, sorted by name         |
| GET    | /persons/{id}         | ADMIN, USER | Get person (USER: own profile only)      |
| PATCH  | /persons/{id}/name    | ADMIN, USER | Update name (USER: own name only)        |
| PATCH  | /persons/{id}/email   | ADMIN       | Update email (cannot update own email)   |

---

## Example API Calls

### Login as admin and save token

See `postman/backend.postman_collection.sh` for the full list of example requests.

---

## Architecture Decisions

### Two separate user stores

`identity-service` holds a `users` table (email + password + role). `main-service` holds a `persons` table (name + email + role + company). They share the same PostgreSQL instance but are logically isolated — each service manages its own schema and owns its data.

When a person's email is updated in `main-service`, a sync call is made to `identity-service` to keep the login credential consistent.

### Remote token validation

`main-service` does not hold the JWT secret. On every authenticated request it calls `GET /auth/validate` on `identity-service`. This means:
- A single source of truth for token validation
- Easier to invalidate tokens centrally in the future
- Slight latency overhead per request (acceptable for this scale)

### DDL auto-update

`spring.jpa.hibernate.ddl-auto=update` is used for simplicity. In production this would be replaced with a migration tool such as Flyway or Liquibase.

### Permission enforcement

Coarse-grained access (ADMIN-only endpoints) is enforced at the controller layer via `@PreAuthorize("hasRole('ADMIN')")`. Fine-grained rules (e.g. USER can only update their own name, nobody can update their own email) are enforced in the service layer and are covered by unit tests.

---

## Assumptions and Tradeoffs

| Topic | Decision |
|---|---|
| Single DB instance | Both services share one PostgreSQL container. In a real deployment they would have separate databases. |
| No token revocation | JWT tokens are stateless; there is no token blacklist or refresh mechanism. |
| Plain inter-service calls | `PATCH /auth/users/email` has no dedicated authentication between services. In production this should use a shared secret or mTLS. |
| Schema management | Hibernate `ddl-auto=update` creates and updates tables automatically. |
| Admin bootstrap | A default admin user is seeded at startup if it does not already exist. |
