# Architecture Overview

This document describes the high‑level design of the Infraimatic platform as
implemented in this repository. It captures key decisions, assumptions and
trade‑offs made during development.

## Multi‑module layout

Infraimatic is organised as a Maven multi‑module project. Each module has a
well‑defined responsibility and a clear set of dependencies:

* **common** – shared data transfer objects, exceptions and minor utilities. It
  contains no Spring or persistence logic and can be reused by any module.
* **domain** – domain model enumerations and value types. Keeping these in
  their own module avoids accidental coupling to infrastructure.
* **infra** – persistence layer including JPA entities, repositories and
  Flyway migrations. This module depends only on `common` and Spring Data JPA.
* **templates** – template management services and REST controllers. Depends
  on `infra` for persistence and `common` for DTOs.
* **submissions** – validation and persistence of normalized submissions.
  Depends on `infra`, `templates` and `common`.
* **nlu** – natural language understanding services and controllers. Relies on
  `templates` to discover the latest template version and `submissions` to
  persist parsed submissions. A simple regex‑based parser is provided. This
  module defines a pluggable `EnrichmentService` interface to allow future
  integration of more advanced ML/LLM models.
* **configuration** – centralised Spring configuration, including security,
  datasource and any shared beans. The security configuration uses a
  symmetric JWT resource server for simplicity. In production this can be
  replaced by integrating Spring Authorization Server or an external OAuth2
  provider.
* **api** – Spring Boot bootstrap module that assembles the other modules and
  exposes the REST API. It includes OpenAPI generation, actuator endpoints
  and integration tests.

## Multi‑tenancy

Multi‑tenancy is implemented using a shared schema model: all tenant data
lives in the same database tables and is scoped by a `tenant_id` column on
each row. Template definitions are unique per `(tenant_id, type, version)`
combination. When persisting submissions the service checks that the
requested template exists for the given tenant and version.

No separate `tenants` table is included by default. Tenants are implicitly
created when their first template is inserted. A tenants table can be
added later to store metadata such as tenant name, contact information and
allowed scopes.

## Security

The project uses Spring Security with the OAuth2 resource server. In
development and tests security can be disabled by setting
`infraimatic.security.enabled=false`. When enabled, incoming requests must
present a bearer token signed with the secret configured at
`infraimatic.security.jwt.secret`. The `SecurityConfig` sets up a
`NimbusJwtDecoder` for HS256 validation. Endpoints under `/actuator/health` and
Swagger UI are publicly accessible. All other endpoints require
authentication. Tenant scoping based on JWT claims is not enforced by
default but can be added by extracting the `tenant` claim from the JWT in a
`OncePerRequestFilter`.

## Validation

Each template definition carries a JSON Schema stored in the database as
`jsonb`. Prior to persisting a submission the `SubmissionService` uses the
networknt JSON Schema validator to ensure the normalized payload conforms to
the schema. Validation errors are collected and returned to callers as
structured messages. Additional validation (e.g. uniqueness constraints) can
be added in the service layer.

## Observability

Spring Boot Actuator exposes health checks, metrics and Prometheus scrapes
when the application is running. Springdoc OpenAPI generates interactive API
documentation served under `/swagger-ui.html`. Logback uses the default
Spring Boot configuration; structured JSON logging can be enabled by
providing a custom `logback-spring.xml` file.

## Known limitations

* **NLU parsing** – The current `EnrichmentServiceImpl` uses a simple regex
  and always recommends the `tt_movement_register` template. In a real
  deployment this component should be replaced by a proper NLP/LLM model
  capable of intent classification and entity extraction.
* **Tenant enforcement** – The JWT does not yet convey tenant information.
  Access control based on tenant membership and roles should be added in a
  future iteration.
* **Pagination** – Listing submissions returns all records for a
  tenant/template. For large datasets pagination should be implemented.
