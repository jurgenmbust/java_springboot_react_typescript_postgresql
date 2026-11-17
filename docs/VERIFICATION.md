# Verification report

Checks executed on November 17, 2026 in the prepared Windows checkout. This report records observed results rather than assuming that generated code works.

| Check | Result |
| --- | --- |
| Java package compilation with JDK 25.0.5 and Maven 3.9.16 | Passed |
| Maven verify, JUnit 5.14.4 | 21 tests passed, 0 failures/errors/skips |
| Product service rules | 6 tests passed |
| Category service rules | 5 tests passed |
| DTO validation | 2 tests passed |
| Full Spring API/repository integration on PostgreSQL 17 | 7 tests passed |
| Generic error privacy | 1 test passed |
| TypeScript check and Vite production build | Passed |
| Vitest / Testing Library | 10 tests passed across 3 files |
| Docker Compose configuration | Passed with config --quiet |
| Multi-stage image builds | Backend and frontend built successfully |
| Compose startup | PostgreSQL, Java and Nginx all healthy |
| Frontend, Swagger UI, OpenAPI through Nginx | HTTP 200 |
| Direct React route /products | HTTP 200 with SPA fallback |
| OpenAPI | Five documented path patterns, all CRUD methods included |
| Browser CRUD | Category/product create, read, edit and delete verified |
| Category deletion guard | Visible 409 message while a product references it |
| Dashboard calculation | 19.99 × 4 produced 79.96, low-stock count 1 |
| Theme and mobile | Light/dark and 390px navigation checked; table scrolling corrected and rechecked |
| Persistence | Product ID and edited quantity survived stop/start of PostgreSQL and Java |
| Public-file credential check | Generated local password absent from publishable files |
| Ignore rules | Environment, keys, tools/builds excluded; example, lockfile and wrapper included |
| npm audit --omit=dev | 0 vulnerabilities |
| Full npm audit | Six high-severity propagated development-tool entries, documented in README |

## What integration tests exercise

Testcontainers starts actual PostgreSQL 17, not H2 or an in-memory substitute. Spring starts the application context, Flyway creates the schema, and Hibernate validates it. MockMvc exercises all product/category CRUD routes, filters, pagination, sorting, duplicate handling, category deletion rules, malformed requests, invalid IDs, fractional quantities, unsupported HTTP methods and OpenAPI generation. Repository calls verify PostgreSQL uniqueness/check constraints independently of service pre-checks.

## What was not verified

- A separately installed native PostgreSQL service and the host-development startup instructions were not executed end to end. The complete application was verified through Docker; Java compilation/tests and frontend builds/tests ran on the host.
- No cloud deployment, public access-control configuration, CI pipeline, container vulnerability scan or Java dependency security scan was performed.
- Browser verification was interactive, not a committed Playwright regression suite.
- No claim is made that dependencies are free of all vulnerabilities. The known frontend development-tool advisory remains documented.

## Delivery state

Temporary browser test records were deleted through the interface, leaving zero products and categories. The Compose stack was stopped with `docker compose down`, preserving the named PostgreSQL volume. No application server is intentionally left running. Docker Desktop remains available. Run the startup commands in the README when ready.

No GitHub repository was created or published. The workspace did not begin as a Git repository, so no commits were created. A disposable ignored Git directory was used only to test exclusion rules; it contains no staged project data. Source files, documentation, local tooling and verification outputs for this application are inside the requested project directory.

The ignored `.tools/dashboard-verification.jpg` is a local screenshot captured during verification with a temporary record. The README screenshot section remains reserved for future portfolio screenshots.
