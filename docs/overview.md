# Infraimatic Platform — overview and architecture

Infraimatic is a modular, multi‑tenant automation platform that ingests and
processes dynamic forms for infrastructure and industrial sites.  It supports
dynamic **template management**, schema validated **submissions**, and a
pluggable **natural language understanding (NLU)** engine to parse free text
into structured records.  The design follows a layered, domain‑driven
architecture: an **API** module bootstraps the Spring Boot application and
delegates work to modules for configuration, templates, submissions and NLU.
Persistence and domain abstractions live in separate modules to avoid
accidental coupling【115756199499176†L8-L37】.  A multi‑tenant model scopes all data
by `tenant_id` so each tenant’s templates and submissions are isolated
【115756199499176†L39-L45】.

![Architecture diagram](architecture.png)

## Module responsibilities

The table below summarises the purpose of each Maven module in the
Infraimatic project.  Keeping responsibilities separate allows each module to
evolve independently and reduces coupling between layers.

| Module | Key responsibilities |
|-------|----------------------|
| **common** | Defines shared DTOs, exceptions and small utilities.  It has no Spring or persistence dependencies and can be reused by any other module【115756199499176†L11-L17】. |
| **domain** | Contains domain enums and value types (e.g., `SubmissionStatus`, `AuditActionType`).  Keeping domain types separate avoids leaking persistence concerns【115756199499176†L14-L15】. |
| **infra** | Houses JPA entities, Spring Data repositories and Flyway migrations.  This module depends only on `common` and Spring Data JPA【115756199499176†L16-L18】. |
| **templates** | Provides services and controllers for managing versioned templates.  Depends on `infra` for persistence and `common` for DTOs【115756199499176†L19-L20】. |
| **submissions** | Validates and persists normalized submissions.  Depends on `infra`, `templates` and `common`【115756199499176†L21-L22】. |
| **nlu** | Implements natural language parsing and submission.  Relies on `templates` to discover the latest version and `submissions` to persist parsed records【115756199499176†L23-L27】. |
| **configuration** | Centralises Spring configuration such as security, datasource and shared beans【115756199499176†L29-L32】. |
| **api** | Bootstraps the Spring Boot application.  Wires all other modules together, exposes REST controllers and generates OpenAPI documentation【115756199499176†L34-L37】. |

## Multi‑tenancy

Infraimatic uses a shared‑schema multi‑tenant model: all data lives in the same
tables but is scoped by a `tenant_id` column【115756199499176†L39-L45】.  Templates
are identified by the tuple `(tenantId, type, version)` and submissions always
reference an existing template version.  This design avoids the complexity of
separate schemas per tenant while still isolating data.  When a new template
is created for a tenant, the next version number is automatically assigned by
the `TemplateService`.

Tenants are implicitly created when their first template is inserted; there is
no explicit `tenants` table by default【115756199499176†L46-L50】.  You can add
such a table later to store metadata such as tenant names and contact
information.

## Security

Infraimatic uses Spring Security’s OAuth2 resource server.  In development and
test profiles you can disable security by setting
`infraimatic.security.enabled=false`【115756199499176†L55-L57】.  When enabled,
all requests require a bearer token signed with the secret configured at
`infraimatic.security.jwt.secret`【115756199499176†L55-L58】.  Endpoints under
`/actuator/health`, Prometheus scrapes and Swagger UI are publicly accessible,
while all other endpoints require authentication【115756199499176†L59-L62】.

To generate a test JWT in development you can use the `io.jsonwebtoken`
library.  The README provides a sample snippet that sets the subject,
tenant claim and expiration【966698866192483†L96-L123】.  Include the token in
the `Authorization` header as `Bearer <token>` when calling the API.

## Observability and configuration

Spring Boot Actuator exposes health checks and metrics; OpenAPI documentation
is generated and served under `/swagger-ui.html`【115756199499176†L75-L80】.
Configuration such as data source and JWT secret can be supplied via
environment variables or `application.yml`.  When using Docker Compose the
`docker-compose.yml` sets these values and exposes the API on port 8080.  You
can also run the application locally with Maven:

```bash
mvn -pl api -am spring-boot:run
```

## Extending the platform

The modular design makes it straightforward to extend Infraimatic.  You can
add new template types by defining a JSON Schema and inserting it via the
template API (see the next section).  The current NLU implementation uses a
simple regex and always recommends the `tt_movement_register` template【115756199499176†L85-L87】; you can
replace `EnrichmentServiceImpl` with a proper ML/LLM model to perform
intent classification and entity extraction.
