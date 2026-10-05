# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Java + Node.js monorepo for an ERP system. `catalog-service` and `stock-service` are Java/Quarkus (Maven) microservices at the repository root; `frontend` (Modern.js/React) and `apps/buy-service` (Node/NestJS) are the Node side, orchestrated with Turbo/Yarn workspaces. Services communicate via RabbitMQ events and expose REST APIs.

`catalog-service` and `stock-service` were migrated big-bang from an earlier Go implementation (DDD + GORM + a shared `go-common` library). That Go code, `go.work`, and `libs/go-common` have been removed from the working tree; the Go history is still recoverable via `git log`/`git show` if ever needed for behavioral reference. Both new services implement the full spec in `.kiro/specs/catalog-stock-crud/requirements.md` (paginated listings, PUT/PATCH/DELETE with lifecycle and referential-integrity rules, domain events, standardized error responses, transactional outbox).

## Project Layout

```
erp/
├── catalog-service/       # Java/Quarkus microservice (products, categories) — port 8080
├── stock-service/         # Java/Quarkus microservice (stock management) — port 8081
├── frontend/               # Modern.js/React app — port 3000
├── apps/
│   └── buy-service/       # Node/NestJS microservice — out of scope for the Java migration
├── libs/
│   └── ts-common/         # Shared TypeScript package (@cms/ts-common), used by frontend
├── .devops/
│   ├── docker/            # Dockerfiles (frontend); catalog/stock now ship their own
│   │                       # Dockerfile.jvm under <service>/src/main/docker/
│   └── k8s/               # Kubernetes manifests
├── package.json           # Node.js workspace root (Yarn), workspaces: apps/*, frontend, libs/*
├── dev.docker-compose.yaml
└── Makefile
```

## Development Commands

### Start Local Infrastructure

```bash
docker compose -f dev.docker-compose.yaml up -d
# Starts: PostgreSQL 15 (5432), RabbitMQ (5672, UI: 15672), PgAdmin (5050)
```

### Java Services (catalog-service & stock-service)

Each service is a standalone Maven/Quarkus project (Java 21). **Use a Java 21 JDK to build/run** — newer JDKs (e.g. 25) currently break the Quarkus/Hibernate bytecode enhancement step (Byte Buddy incompatibility). Point `JAVA_HOME` at a JDK 21 install before running `mvn`.

```bash
# Hot-reload dev mode (from the service directory, or via the Makefile)
cd catalog-service && mvn quarkus:dev
cd stock-service && mvn quarkus:dev

# Or via Makefile
make catalog   # mvn quarkus:dev for catalog-service
make stock     # mvn quarkus:dev for stock-service
make build     # mvn package for both services

# Build a jar directly
cd catalog-service && mvn package   # -> target/quarkus-app/quarkus-run.jar
```

Each service reads Postgres/RabbitMQ connection settings from environment variables (see below), with dev-friendly defaults baked into `src/main/resources/application.properties`. Flyway migrations (`src/main/resources/db/migration`) own the schema — Hibernate ORM does not auto-generate DDL. OpenAPI/Swagger UI is available at `/docs` on each service.

### Frontend

```bash
cd frontend
yarn install
yarn dev       # Modern.js dev server
yarn build
yarn serve     # preview production build
```

### Monorepo (Turbo)

```bash
yarn dev       # start all Node workspaces (frontend, apps/buy-service)
yarn build     # build all Node workspaces
```

## Architecture

### Java Microservices — Internal Structure

Both `catalog-service` and `stock-service` follow the same Domain-Driven Design layering (mirroring the DDD split used by the original Go services, reimplemented independently per service — there is no shared Java library):

```
src/main/java/software/openlab/<catalog|stock>/
├── domain/<entity>/          # Domain objects, repository interfaces, event payloads
├── domain/shared/            # Id records, ApiException hierarchy (BadRequest/NotFound/Conflict/...), PageResult, EventPublisher
├── application/usecase/<entity>/  # One class per operation (CreateXUseCase, GetXByIdUseCase, ListXUseCase, UpdateXUseCase,
│                                    # DeleteXUseCase, ...), each with a single `execute(...)` method — no umbrella *Service interface
└── infra/
    ├── rest/              # JAX-RS resources + DTOs + JSON exception mappers
    ├── persistence/        # Hibernate ORM with Panache entities/repositories
    └── messaging/          # Outbox event publisher + RabbitMQ publisher + the outbox relay job
src/main/resources/
├── application.properties
└── db/migration/          # Flyway migrations (schema is "catalog" / "stock")
```

Key patterns:
- Repository interfaces are defined in `domain/`, implemented in `infra/persistence/`.
- Each business operation is its own `@ApplicationScoped` use case class (e.g. `application/usecase/product/UpdateProductUseCase`) injected directly by REST resources and messaging handlers; use cases may compose other use cases (e.g. `UpdateProductUseCase` calls `GetProductByIdUseCase`) instead of duplicating repository lookups.
- Domain aggregates use Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) instead of hand-written boilerplate; a constructor stays hand-written wherever it isn't a pure all-fields assignment (e.g. it calls `super(...)` or computes a field).
- Public IDs are prefixed and validated (`cat_*`, `prod_*`, `stock_*`, ...) — see each service's `domain/shared`/`domain/<entity>` id records (`CategoryId`, `ProductId`, `StockId`, `ReassignmentId`).
- Domain events (e.g. `category.created`, `product.updated`) are written to an `outbox_entries` table in the **same transaction** as the aggregate write (`@Transactional` service methods), guaranteeing atomicity. A `@Scheduled` job in `infra/messaging` (the "relay") polls that table and publishes pending entries to RabbitMQ asynchronously — this replaces the Go version's separate `relay` binary with an in-process job, a deliberate simplification.
- Standardized error responses are produced by JAX-RS `ExceptionMapper`s in `infra/rest/exception`: business errors (400/404/409) return `{"message": "..."}`; bean-validation failures return `{"mensagem": "...", "erros": {...}}`.

### Event Flow

Services publish domain events to the RabbitMQ topic exchange `catalog.events` / `stock.events` after state mutations, via the transactional outbox described above. Other services subscribe to relevant events (e.g. `stock-service` reacts to `product.updated`/`product.deleted`).

### Frontend

Modern.js 3 (React 19, TypeScript). SSR enabled. Linting via Biome (2-space indent, single quotes, 80-char line width). Uses `@cms/ts-common` for shared types.

## Environment Configuration

Each Java service reads Postgres/RabbitMQ settings from environment variables (with local-dev defaults in `application.properties`):

```
RABBITMQ_HOST / RABBITMQ_PORT / RABBITMQ_USER / RABBITMQ_PASSWORD
POSTGRES_HOST / POSTGRES_PORT / POSTGRES_DATABASE / POSTGRES_USER / POSTGRES_PASSWORD
```

Default dev credentials are in `dev.docker-compose.yaml`.

## Tooling Notes

- **Maven / Quarkus** — `mvn quarkus:dev` for hot reload, `mvn package` to build `target/quarkus-app/quarkus-run.jar`. Requires a Java 21 JDK on `JAVA_HOME`/`PATH`.
- **Flyway** — owns the Postgres schema for each service; migrations live in `src/main/resources/db/migration`.
- **smallrye-openapi / swagger-ui** — OpenAPI docs served at `/docs` on each service (equivalent to the old `swag`-generated docs).
- **Biome** — Frontend lint/format; run `biome check` in `frontend`.
- **Turbo** — Caches build outputs in `.next/**` and `dist/**`; `dev` task is non-cached and persistent.
