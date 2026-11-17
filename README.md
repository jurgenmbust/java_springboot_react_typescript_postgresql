# Inventory Management System

A full-stack inventory application built to learn modern Java and Spring Boot while producing a readable portfolio project. Manage products, categories, prices and stock through a responsive React dashboard backed by a real PostgreSQL database.

The interface is branded **Stockroom**; the application name is **Inventory Management System**. It starts with an empty database. Production screens never substitute mock data for API responses.

## Features

- Complete product and category CRUD with validated forms and deletion confirmations.
- Case-insensitive unique category names and normalized uppercase unique SKUs.
- Product name search, category and low-stock filters, pagination, and name/price/creation-date sorting.
- Dashboard totals, inventory value, low-stock count, and up to five recent/low-stock products.
- Server-side validation, structured errors, transaction boundaries, and database constraints.
- Decimal money stored as PostgreSQL `NUMERIC(14,2)` and Java `BigDecimal`; API money values are strings.
- Responsive navigation, light/dark themes, keyboard-friendly dialogs, loading/empty/error states, and success notifications.
- Flyway migrations, generated OpenAPI/Swagger UI, Maven Wrapper, and automated tests.
- Multi-stage Docker builds, health checks, an internal database network, and persistent storage.

## Tech stack and versions

Backend: **Java 25 LTS**, **Spring Boot 4.1.2**, Spring Web MVC, Spring Data JPA, Hibernate **7.4.13.Final**, Jakarta Validation, PostgreSQL JDBC, Flyway, Springdoc **3.2.0**, JUnit **5.14.4**, Mockito and Testcontainers. Maven Wrapper uses **3.9.16**. Spring Boot manages transitive backend versions; see `backend/pom.xml` and its effective dependency tree.

PostgreSQL uses the **17-alpine** Docker image. Runtime images use Java 25 JRE, Node 22 for frontend compilation, and unprivileged Nginx for serving static files. Docker minor tags can receive patches; use image digests if you need bit-for-bit image reproducibility.

| Frontend dependency | Installed version |
| --- | --- |
| axios | 1.21.1 |
| lucide-react | 1.74.0 |
| react | 19.3.1 |
| react-dom | 19.3.1 |
| react-router-dom | 7.18.5 |
| tailwindcss | 4.3.5 |
| typescript | 7.0.2 |
| vite | 8.3.8 |
| vitest | 5.1.0 |

`frontend/package-lock.json` locks the frontend dependency tree. Versions were checked against Maven Central and npm rather than guessed. Spring Boot 4 normally manages JUnit 6; this project explicitly selects JUnit 5 to match the learning requirements, and its test suite runs on that version.

Official compatibility references: [Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html), [Springdoc compatibility](https://springdoc.org/), [React](https://react.dev/learn).

## Project structure

```text
java_springboot_react_typescript_postgresql/
├── backend/
│   ├── src/main/java/com/inventory/
│   │   ├── controller/     # HTTP routing and status codes
│   │   ├── service/        # Business rules and transactions
│   │   ├── repository/     # Spring Data database operations
│   │   ├── entity/         # JPA mappings
│   │   ├── dto/            # Validated inputs and JSON outputs
│   │   ├── exception/      # Shared error responses
│   │   └── config/         # OpenAPI configuration
│   ├── src/main/resources/db/migration/
│   ├── src/test/java/com/inventory/
│   ├── .mvn/              # Official Maven Wrapper configuration
│   ├── mvnw / mvnw.cmd
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/components/    # Forms, dialogs, notifications, feedback
│   ├── src/pages/         # Dashboard, products, categories
│   ├── src/services/      # Axios client and typed HTTP operations
│   ├── src/hooks/         # Cancellable data loading and debounce
│   ├── src/types/         # Request and response models
│   ├── src/layouts/       # Sidebar, navigation, theme
│   ├── src/test/          # Vitest and Testing Library tests
│   ├── nginx.conf
│   └── Dockerfile
├── docs/                  # Architecture, API and verification notes
├── scripts/start-backend.ps1
├── .env.example
├── .gitignore
├── docker-compose.yml
└── docker-compose.dev.yml # Optional database port for host development
```

`.tools/` may exist in this checkout for portable build tools and verification output. It is ignored and is not required when cloning the repository.

## Prerequisites

**Docker route:** Docker Engine/Desktop running Linux containers and Docker Compose v2. Java, Maven and Node are included in the build images, so you do not need to install them on the host.

**Host development:** JDK 25, Node.js 22.20+ with npm, and PostgreSQL 17. Maven Wrapper downloads Maven automatically. Backend integration tests also require a running Docker engine to start isolated PostgreSQL containers.

Check tools with `docker compose version`, `java -version`, and `node --version` as appropriate.

## Getting started with Docker

Replace `<your-repository-url>` with the URL of the repository you publish. No repository has been published automatically.

```powershell
git clone <your-repository-url>
cd <repository-directory>
```

If this project is a subdirectory of your repository, enter it:

```powershell
cd java_springboot_react_typescript_postgresql
```

If this directory itself is your repository root, remain there. Then:

```powershell
Copy-Item .env.example .env
# Edit .env and replace POSTGRES_PASSWORD with a strong local password.
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

On macOS/Linux use `cp .env.example .env` for the copy step. A configured `.env` already exists in the prepared local checkout, with a randomly generated password that is never part of tracked files. Copying the example over it replaces that configuration; do this only when you intend to reset the settings.

Wait for all three services to be healthy. The first build downloads Maven/npm dependencies and images.

- Frontend: **http://localhost:8080**
- API: **http://localhost:8080/api/products**
- Swagger UI: **http://localhost:8080/swagger-ui/index.html**
- OpenAPI JSON: **http://localhost:8080/v3/api-docs**

Create a category before adding products. All prices display in USD. Low stock means `quantity <= LOW_STOCK_THRESHOLD`, including zero.

Stop the application without removing data:

```powershell
docker compose down
```

Database records remain in the named `postgres_data` volume. Avoid `docker compose down -v` unless you explicitly want to erase the local database. Changing PostgreSQL credentials in `.env` does not change users in an already initialized volume; update the database role separately or deliberately recreate a disposable volume.

## Running without Docker

### 1. Start PostgreSQL

Install PostgreSQL 17 using the official installer/package manager and start its service. Using a local administrator connection, create the role and database:

```sql
CREATE ROLE inventory_user LOGIN;
```

In `psql`, use `\password inventory_user` to set the password interactively, then:

```sql
CREATE DATABASE inventory_db OWNER inventory_user;
```

Use the same database name, role and password in `.env`. Set `DB_HOST=localhost` and the actual `DB_PORT`. Flyway creates the application tables at first startup; do not create those tables manually.

If you prefer only the database in Docker while developing Java/React on the host:

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d db
```

This override exposes PostgreSQL only at `127.0.0.1:DB_PORT`. It is intentionally absent from the default three-container setup. Stop an existing full stack before switching development modes to avoid confusing two API processes that share the database.

### 2. Configure and start the backend

Install JDK 25 and set `JAVA_HOME` if necessary. From the project root in PowerShell:

```powershell
Copy-Item .env.example .env  # Only if .env does not already exist.
# Edit the connection settings before starting.
.\scripts\start-backend.ps1
```

The script loads `.env` into the current backend process and runs `backend/mvnw.cmd spring-boot:run`. Spring Boot does **not** automatically read a root `.env`; the script bridges that gap. Values in `.env` should be unquoted single-line assignments. If PowerShell policy prevents local scripts, load those variables into your terminal yourself and run `./mvnw.cmd spring-boot:run` from `backend`.

For a POSIX shell, with a trusted `.env` containing shell-safe values:

```sh
set -a
. ./.env
set +a
cd backend
sh ./mvnw spring-boot:run
```

Do not source untrusted `.env` files. Set `JAVA_HOME` to your Java 25 installation.

Default backend: **http://localhost:8081**. Development Swagger: **http://localhost:8081/swagger-ui/index.html**. The default `dev` profile is a label for local configuration; it does not add sample data or disable validation.

### 3. Install and start the frontend

In another terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open **http://localhost:5173**. Vite reads the public `VITE_API_BASE_URL` from the root `.env` and proxies `/api` to the backend using `BACKEND_PORT`. Restart Vite after changing environment variables. The proxy keeps browser requests on one origin, so CORS is unnecessary for this setup.

Compile the frontend with `npm run build`. Vite preview serves compiled frontend files only; the Docker/Nginx route is the supported full-stack compiled preview.

## Environment variables

| Variable | Default/example | Purpose |
| --- | --- | --- |
| POSTGRES_DB | inventory_db | Database name |
| POSTGRES_USER | inventory_user | Database role; required by Spring |
| POSTGRES_PASSWORD | placeholder only in example | Database password; required, never public |
| DB_HOST | localhost | Database host for host development; Compose uses db |
| DB_PORT | 5432 | Database port for host development; Compose uses 5432 internally |
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile |
| VITE_API_BASE_URL | /api | Public API base path, embedded in frontend build |
| APP_PORT | 8080 | Loopback-only frontend port published by Compose |
| BACKEND_PORT | 8081 | Host Java port and Vite proxy target; Compose keeps 8081 internally |
| LOW_STOCK_THRESHOLD | 5 | Non-negative inclusive low-stock limit |

Do not put secrets in `VITE_*`: frontend build variables are public. Default Docker networking publishes only the frontend port. PostgreSQL and Java are reachable through their internal networks. Backend and frontend images run as non-root users. Spring health details are hidden.

`.gitignore` excludes `.env`, local tools, build artifacts, IDE files and private keys. Keep `.env.example`, Maven Wrapper and `package-lock.json` in the repository. No authentication or authorization is implemented: this version is for local use and trusted environments. Do not expose it publicly before adding appropriate access control, TLS, deployment configuration and operational monitoring.

## API endpoints

| Method | Path | Description |
| --- | --- | --- |
| GET | /api/products | Search/filter/sort/page products |
| GET | /api/products/{id} | Read one product |
| POST | /api/products | Create a product (201 + Location) |
| PUT | /api/products/{id} | Replace editable product fields |
| DELETE | /api/products/{id} | Delete a product (204) |
| GET | /api/categories | List categories alphabetically |
| GET | /api/categories/{id} | Read one category |
| POST | /api/categories | Create a category (201 + Location) |
| PUT | /api/categories/{id} | Replace editable category fields |
| DELETE | /api/categories/{id} | Delete an empty category (204) |
| GET | /api/dashboard | Totals, recent products and low-stock products |

Example query: `/api/products?search=keyboard&categoryId=1&lowStock=true&page=0&size=10&sort=price&direction=asc`.

Read [docs/API.md](docs/API.md) for payloads, constraints, pagination and errors. Swagger documents the actual DTO shapes and parameters.

## Database

One category has many products; each product references exactly one valid category through `category_id`. The foreign key restricts category deletion while products exist. SKU uniqueness, case-insensitive category-name uniqueness, and non-negative numbers are enforced by PostgreSQL as well as service/validation rules. There is no cascade deletion.

Money is exact decimal data. Quantity is a 32-bit integer. Dashboard inventory value is computed with `SUM(price * quantity)` in PostgreSQL. Timestamps use UTC and are returned as ISO local-date-time strings; the frontend interprets them as UTC.

Flyway runs `V1__create_inventory.sql` once and tracks its checksum. Add `V2__...sql` for future schema changes instead of editing an applied migration. Hibernate uses `ddl-auto: validate` to detect schema/mapping mismatches. Category, normalized name, creation date, price and quantity indexes support common queries. A leading-wildcard name search may scan records; for a much larger catalog, add a PostgreSQL trigram index after measuring the workload.

## Testing

From `backend` with JDK 25 and Docker running:

```powershell
.\mvnw.cmd verify
```

On macOS/Linux use `sh ./mvnw verify`. Tests create their own PostgreSQL 17 container, apply Flyway, and remove the container afterwards. They do not connect to the `.env` application database. Docker must be available; integration tests are not silently skipped.

Backend tests cover product/category business rules, DTO validation, HTTP CRUD/filtering, duplicate and missing records, database constraints, structured errors and generated OpenAPI. Mockito isolates service tests; MockMvc exercises the real Spring application against PostgreSQL for integration tests. JUnit 5 is explicitly configured.

From `frontend`:

```powershell
npm ci
npm test
npm run build
```

Frontend tests cover rendered dashboard states, product/category form validation, successful form payloads, preservation of drafts after API errors, and centralized Axios error handling. Test-only data stays under `src/test` and is never used by production pages.

See [docs/VERIFICATION.md](docs/VERIFICATION.md) for checks actually executed in the prepared environment.

## Architecture and learning path

```text
Browser / React + TypeScript
          | Axios / JSON
          v
Vite proxy (development) or Nginx (Docker)
          |
          v
Spring MVC Controller -> Service -> Spring Data Repository
                                      |
                                 Hibernate / JDBC
                                      |
                                  PostgreSQL
```

Controllers translate HTTP into use cases. Services validate business rules and define transactions. Repositories handle persistence. DTOs keep the HTTP contract separate from JPA entities. React components render server data and keep editable form state local.

Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for simple explanations and comparisons with ASP.NET Core and Entity Framework.

Suggested study order: follow a product creation from `ProductForm.tsx` through `inventory.ts`, `ProductController`, `ProductServiceImpl`, `ProductRepository`, then inspect the SQL migration. Read and run one test at each layer. As an exercise, add a reorder-level field using a new migration, DTO validation and frontend input.

## Dependency audit

The initial frontend audit reports six high-severity entries propagated from the same `picomatch` ReDoS advisory, [GHSA-fc5q-39xc-vjpf](https://github.com/advisories/GHSA-fc5q-39xc-vjpf). The registry's latest available `picomatch` is affected. Entries involve Vite, its React plugin and Vitest tooling; these dependencies are used to build/test and are absent from the final Nginx image. The runtime-only npm audit is clean. Review `npm audit` and update when a compatible correction is available. Do not apply forced major-version downgrades blindly.

This check does not replace Java dependency or container-image vulnerability scanning; those scans have not been configured here.

## Future improvements

- Authentication and user roles.
- Inventory movement history and audit logging.
- CSV export and bulk imports.
- Optimistic locking for concurrent edits.
- Cloud deployment, image scanning and CI/CD pipelines.
- Trigram search and category pagination for larger datasets.

## Screenshots

Reserved for screenshots after you review the interface and add portfolio data. No screenshots are claimed or embedded here yet.

## Troubleshooting

- Start Docker Desktop in Linux-container mode before Compose or integration tests.
- Use `docker compose ps` and `docker compose logs --tail=100 backend` for startup failures. Do not share logs or resolved Compose output without checking for private settings.
- Use `docker compose config --quiet` to validate without printing interpolated credentials.
- Change `APP_PORT` if 8080 is occupied; use the chosen port in frontend/Swagger URLs.
- If host PostgreSQL already uses 5432, choose another `DB_PORT` for the development override.
- A changed Flyway V1 checksum usually means an applied migration was edited. Restore V1 and create V2.
- If Java compilation fails, verify JDK 25 is selected by `JAVA_HOME`; a JRE alone cannot compile the project.
