# Architecture and learning guide

## The request lifecycle

A user submits a React form. Axios serializes the input as JSON. Vite or Nginx forwards the request to Spring MVC. The controller validates the DTO and calls a service. The service applies business rules and asks a repository to read/write data. Hibernate translates entity operations into SQL, and PostgreSQL enforces the schema. The service maps the result to a response DTO. React updates after the API confirms success.

## Spring Boot

`InventoryApplication` is the entry point. `@SpringBootApplication` enables automatic configuration and component scanning under `com.inventory`. Spring starts an embedded web server and configures common infrastructure from the classpath and `application.yml`.

In ASP.NET Core, compare this entry point and configuration to `Program.cs`, application builder setup, service registration and application settings. Spring performs more registration through annotations and package scanning.

## Controller layer

`@RestController` marks classes that return HTTP response data. `@RequestMapping` defines the shared route prefix. `@GetMapping`, `@PostMapping`, `@PutMapping` and `@DeleteMapping` select HTTP methods. `@RequestBody` reads JSON, and `@Valid` runs validation rules on request DTOs. `@Positive` rejects invalid path IDs.

Controllers handle HTTP concerns: routes, request parameters, response status and Location headers. They delegate business decisions to services. This is similar to ASP.NET Core API controllers with `[ApiController]`, route attributes and injected application services.

## Service layer

`@Service` registers a business-logic component. Interfaces describe available operations; implementation classes contain the rules. `ProductServiceImpl` checks SKU uniqueness, resolves the category and applies changes. `CategoryServiceImpl` prevents deleting categories that still contain products.

`@Transactional` groups related database operations. A write transaction commits as a unit, or rolls back if a runtime exception occurs. Read-only transactions provide the persistence context needed while mapping lazy category relationships. `DashboardService` uses repeatable-read isolation so several aggregate queries see a consistent snapshot.

A uniqueness pre-check gives a useful message, but two simultaneous requests can pass it. The database unique constraints remain the final protection. A foreign key protects deletion in concurrent cases. Data-integrity violations become HTTP 409. Update conflicts currently use last-write-wins; optimistic locking is a future improvement.

## Repository layer

`JpaRepository<Entity, Long>` supplies CRUD operations. Spring Data generates implementations at startup. Method names such as `existsBySku` describe queries. `JpaSpecificationExecutor` combines optional product filters. The sort field is restricted to an allowlist; SQL LIKE wildcard characters in search input are escaped.

A repository resembles a data-access abstraction around EF Core's `DbSet`. Spring Data repositories are generated interfaces; EF Core typically exposes LINQ queries through `DbContext` directly.

## JPA and Hibernate

JPA is the persistence specification; Hibernate is its implementation. `@Entity` maps a Java class to a table. `@Id` and `@GeneratedValue` describe a database-generated ID. `@Column` describes column mapping. `@ManyToOne` and `@JoinColumn` model the required category foreign key. `@MappedSuperclass` shares timestamp columns. JPA callbacks fill timestamps before inserts and updates.

This resembles EF Core entity classes and mapping configuration. Hibernate's persistence context tracks changes similarly to EF's change tracker. `saveAndFlush` writes SQL early enough to detect constraints and populate timestamps before mapping a response; the transaction still controls final commit.

`open-in-view` is disabled. Controllers cannot accidentally trigger lazy queries during JSON serialization. Mapping DTOs inside services makes data access explicit. This small app can issue extra category reads while mapping a page; consider entity graphs or projections if profiling shows a need to optimize.

## Dependency injection

Spring constructs services/controllers and supplies constructor parameters. The classes do not construct database repositories themselves. This makes dependencies visible and allows tests to pass Mockito mocks. Compare this with constructor injection through ASP.NET Core's built-in container.

## DTOs and validation

Java records define compact request/response data structures. They are not database entities. Validation annotations such as `@NotBlank`, `@Digits` and `@PositiveOrZero` validate incoming data. Services enforce relationships and uniqueness. PostgreSQL enforces rules even if data is written outside this API.

DTOs resemble C# request/response records. They keep clients independent of persistence internals and avoid serializing lazy proxies or relationship cycles.

Money uses `BigDecimal` and PostgreSQL `NUMERIC`, comparable to C# `decimal`. Responses and React inputs use decimal strings. JavaScript `Number` is used only to format display values, never to calculate persisted inventory totals.

## Error handling

`@RestControllerAdvice` centralizes exception translation, similar to an ASP.NET Core exception filter or middleware. `ApiError` includes time, HTTP status, message, path and field errors. Expected missing/duplicate records produce 404/409. Invalid input produces 400. Unexpected failures produce a generic 500 without exposing internal exception messages.

## Database migrations

Flyway applies ordered SQL files and records their checksums in `flyway_schema_history`. Existing migrations must remain unchanged after use. New schema changes receive a new version. Hibernate validates the migrated schema instead of generating production tables. This serves the same purpose as versioned EF Core migrations, although the migration source here is explicit SQL.

## React and TypeScript

React components are functions that describe interface elements with JSX. Props pass data and callbacks from parent to child. `useState` stores values that may change. Updating state causes React to render the relevant component tree again. Inputs are controlled by form state, so the draft remains visible after a failed save.

`useEffect` synchronizes data loading and dialog lifecycle with rendering. `useResource` cancels stale requests using `AbortController`. `useCallback` gives the loader a stable identity until its filters change. `useDebouncedValue` reduces requests while typing.

React Router switches pages without a full reload. TypeScript interfaces describe input and response shapes, but cannot validate external data at runtime; backend validation remains essential. Axios provides a shared base URL, timeout and error interceptor. Context shares success notifications; it does not contain business records.

The native dialog element provides focus trapping and Escape behavior. Forms expose labels and field messages, tables use semantic headers, mobile navigation has an accessible toggle, and motion is reduced when requested. Theme preference is the only data saved in browser local storage; inventory data stays in PostgreSQL.

## Docker communication

The browser sees one loopback-only frontend port. Nginx serves static files and forwards API/Swagger requests to Java. Java connects to PostgreSQL over an internal database network. The database has a named volume and no published port in the default setup. Health checks gate service startup; restart policies recover exited processes.

Frontend and backend use multi-stage builds and non-root runtime users. Build tools and frontend development dependencies do not enter runtime images. A Java Docker build skips tests because host/test-container verification is a separate step; always run the test suite before distributing images.

No authentication is included. This is a local learning topology, not a complete public production security configuration.
