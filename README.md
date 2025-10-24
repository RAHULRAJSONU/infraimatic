# Infraimatic

Infraimatic is a modular, multi‑tenant automation platform designed to ingest
and process dynamic forms at infrastructure sites and industrial plants. It
supports dynamic template management, schema‑validated submissions, and a
pluggable natural language understanding (NLU) engine to parse free text into
structured records. The system employs a shared‑schema multi‑tenant model and
uses OAuth2/JWT for API security.

## Architecture

Infraimatic follows a layered, domain‑driven architecture with separate
Maven modules for each concern:

```
┌────────────────────────────────────┐
│              API (api)            │
│  - Bootstraps Spring Boot app     │
│  - Exposes REST controllers       │
└────────────────────────────────────┘
             ▲            ▲
             │            │
┌────────────┴───────┐┌────┴─────────────┐
│    Configuration   ││       NLU        │
│ (security, beans)  ││ (parse & submit) │
└────────────┬───────┘└────┬─────────────┘
             ▲            ▲
             │            │
┌────────────┴─────┐┌─────┴─────────┐
│    Templates     ││    Submissions │
│  (CRUD, version) ││(validation, DB)│
└────────────┬─────┘└─────┬─────────┘
             ▲            ▲
             │            │
┌────────────┴────────────┴──────────┐
│               Infra                │
│   (JPA entities, repositories)     │
└────────────────────────────────────┘
```

* **common** – shared DTOs, exceptions and utilities
* **domain** – domain enums and core abstractions
* **infra** – JPA entities, repositories and Flyway migrations
* **templates** – services and controllers for managing template definitions
* **submissions** – services and controllers for ingesting submissions and
  validating against template schemas
* **nlu** – pluggable NLU module with a basic regex‑based implementation
* **configuration** – centralised Spring configuration including security
* **api** – boot module that wires everything together and exposes the REST API

## Getting started

### Prerequisites

* Java 25 and Maven 3.9+
* A running PostgreSQL instance (or use Docker Compose)

### Building the project

From the `infraimatic` directory run:

```bash
mvn -T1C -DskipTests=false clean package
```

This will compile all modules, run unit and integration tests (using
Testcontainers) and produce the runnable JAR `api/target/infraimatic-api-0.1.0-SNAPSHOT.jar`.

### Running locally with Docker Compose

A `docker-compose.yml` is provided for convenience. It starts a PostgreSQL
database and the Infraimatic application. Run:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`. Swagger UI is served at
`http://localhost:8080/swagger-ui.html`.

### Running locally without Docker

Ensure Postgres is running and update `src/main/resources/application.yml` with
the correct datasource settings. Then run:

```bash
mvn -pl api -am spring-boot:run
```

### Authentication

Infraimatic uses OAuth2/JWT for API security. In development and test
profiles the security can be disabled by setting:

```properties
infraimatic.security.enabled=false
```

When enabled, the application expects a bearer token signed with the
secret defined in `infraimatic.security.jwt.secret`. You can generate a test
token using the following snippet (requires `io.jsonwebtoken`):

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

String secret = "changeit";
String jwt = Jwts.builder()
    .setSubject("sample_user")
    .claim("tenant", "sample_tenant")
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + 86400000L))
    .signWith(SignatureAlgorithm.HS256, secret.getBytes())
    .compact();
System.out.println(jwt);
```

Include the generated token in the `Authorization` header as
`Bearer <token>` when calling the API.

### API examples

Replace `<token>` with a valid JWT if security is enabled.

#### Parse natural language

```bash
curl -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"text":"Pump-3 moved from Site-A to Site-B for maintenance at 2025-09-28T10:20:00Z"}' \
     http://localhost:8080/api/v1/sample_tenant/nlu/parse
```

#### Submit natural language

```bash
curl -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"text":"Pump-3 moved from Site-A to Site-B for maintenance at 2025-09-28T10:20:00Z"}' \
     http://localhost:8080/api/v1/sample_tenant/nlu/submit
```

#### Get a stored submission

```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/v1/sample_tenant/templates/tt_movement_register/1/submissions/{id}
```

### Development notes

* Flyway migrations reside in `infra/src/main/resources/db/migration`.
* To add a new template type, implement the JSON Schema and insert a
  corresponding row in the `template_definitions` table. Submissions will
  automatically be validated against it.
* The NLU implementation (`nlu` module) currently performs basic pattern
  extraction. Replace `EnrichmentServiceImpl` with a proper ML/LLM based
  implementation by implementing the `EnrichmentService` interface.
* Logging is configured via Spring Boot defaults. Structured logging can be
  enabled by customizing Logback configuration if desired.

### Post‑build checklist

Before delivering this repository the following checks were performed:

1. `mvn clean package` on JDK 25 completes without errors and all tests pass.
2. Flyway migrations apply cleanly against a fresh Postgres database.
3. The seeded tenant `sample_tenant` and TT Movement template are present
   in the database after migrations.
4. Integration tests exercise the NLU parse and submit flows and verify
   persisted records.
5. Swagger UI is available at `/swagger-ui.html` when the application runs.
6. Docker Compose brings up the application and database successfully.

