# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Java + Node.js monorepo for an ERP system. `catalog-service`, `stock-service` and `customer-service` are Java/Quarkus (Maven) microservices at the repository root; `frontend` (Modern.js/React, with a BFF in `frontend/api/`) is the Node side, a standalone app with its own `package.json` (there is no workspace root). Services communicate via RabbitMQ events and expose REST APIs.

`catalog-service` and `stock-service` were migrated big-bang from an earlier Go implementation (DDD + GORM + a shared `go-common` library). That Go code, `go.work`, and `libs/go-common` have been removed from the working tree; the Go history is still recoverable via `git log`/`git show` if ever needed for behavioral reference. Both new services implement the full spec in `.kiro/specs/catalog-stock-crud/requirements.md` (since deleted from the tree; recover it with `git log`) (paginated listings, PUT/PATCH/DELETE with lifecycle and referential-integrity rules, domain events, standardized error responses, transactional outbox).

## Project Layout

```
erp/
├── catalog-service/       # Java/Quarkus microservice (products, categories) — port 8080
├── stock-service/         # Java/Quarkus microservice (stock management) — port 8081
├── customer-service/      # Java/Quarkus microservice (customers: individuals + companies) — port 8082
├── frontend/              # Modern.js/React app, BFF in api/ — port 3000
├── .devops/
│   ├── docker/            # Dockerfile.frontend is the one CI uses. The Java images are built from
│   │                       # <service>/src/main/docker/Dockerfile.jvm; the Dockerfile.<service> files here are not used by CI
│   └── helm/              # Helm chart that ArgoCD deploys (see Delivery)
└── .github/workflows/main.yml   # build + publish pipeline
```

There is no root `package.json`, Turbo, Makefile or compose file anymore. `dev.docker-compose.yaml` was removed (history: `git log -- dev.docker-compose.yaml`).

## Development Commands

### Local Infrastructure

The services default to `localhost` for Postgres (5432, database `erp`) and RabbitMQ (5672); the credentials defaults are in each `application.yaml`. With no compose file in the repo, run Postgres 15 and RabbitMQ yourself (e.g. with Docker) before starting a service.

### Java Services (catalog-service, stock-service & customer-service)

Each service is a standalone Maven/Quarkus project (Java 21). **Use a Java 21 JDK to build/run** — newer JDKs (e.g. 25) currently break the Quarkus/Hibernate bytecode enhancement step (Byte Buddy incompatibility). Point `JAVA_HOME` at a JDK 21 install before running `mvn`.

```bash
# Hot-reload dev mode (from the service directory)
cd catalog-service && mvn quarkus:dev
cd stock-service && mvn quarkus:dev
cd customer-service && mvn quarkus:dev

# Build a jar directly
cd catalog-service && mvn package   # -> target/quarkus-app/quarkus-run.jar
```

Each service reads Postgres/RabbitMQ connection settings from environment variables (see below), with dev-friendly defaults baked into `src/main/resources/application.yaml`. Flyway migrations (`src/main/resources/db/migration`) own the schema — Hibernate ORM does not auto-generate DDL. OpenAPI/Swagger UI is available at `/docs` on each service.

`quarkus.datasource.jdbc.telemetry: true` needs `io.opentelemetry.instrumentation:opentelemetry-jdbc` in the service's `pom.xml`; without it the app fails at startup with `NoClassDefFoundError: OpenTelemetryDataSource`.

### Frontend

```bash
cd frontend
yarn install   # yarn.lock and package-lock.json are both present
yarn dev       # Modern.js dev server
yarn build
yarn serve     # preview production build
yarn lint      # biome check
```

## Delivery

CI never commits back to the repository. Deploying is ArgoCD's job, driven by a Helm chart published to GHCR.

- **Pipeline** (`.github/workflows/main.yml`): pushing a tag `vX.Y.Z` builds and pushes the four images (`ghcr.io/openlab-software/<service>:vX.Y.Z`) and publishes the chart `oci://ghcr.io/openlab-software/charts/erp` with version `X.Y.Z` (`appVersion` = the tag, which is the frontend image tag). PRs only build the images and run `helm lint`/`helm template`; nothing is pushed.
- **Chart** (`.devops/helm`): the Quarkus kubernetes extension generates `target/kubernetes/kubernetes.yml` at build time with the image tag already set. CI copies it to `.devops/helm/files/<service>.yaml` (gitignored) before `helm package`, and `templates/java-services.yaml` adjusts it at render time: renames the shared `view-jobs` Role per service, turns the `*-flyway-init` Job into an ArgoCD `Sync` hook with `activeDeadlineSeconds` (`flyway.activeDeadlineSeconds` in `values.yaml`) and sets the release namespace. The frontend, infra (Postgres, RabbitMQ, the `erp-secrets` ExternalSecret) and observability (ServiceMonitor, Instrumentation) are hand-written templates. `services.<name>.enabled` in `values.yaml` turns each Java service on or off. To run `helm template` locally, put the generated manifests in `.devops/helm/files/` first.
- **ArgoCD**: the ApplicationSet in the `infrastructure` repository discovers repositories that have `.devops/helm` and follows the chart with `targetRevision: "*"` (highest stable version). A new release is picked up on the next refresh.
- **Failed sync**: ArgoCD keeps retrying a failed operation with the revision it started with, so a fixed release does not take over by itself. Terminate the operation and sync again (UI, or clear `operation` on the Application and sync the new revision).

## Architecture

### Java Microservices — Internal Structure

`catalog-service`, `stock-service` and `customer-service` follow the same Domain-Driven Design layering (mirroring the DDD split used by the original Go services, reimplemented independently per service — there is no shared Java library):

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
├── application.yaml
└── db/migration/          # Flyway migrations (schema is "catalog" / "stock")
```

Key patterns:
- Repository interfaces are defined in `domain/`, implemented in `infra/persistence/`.
- Each business operation is its own `@ApplicationScoped` use case class (e.g. `application/usecase/product/UpdateProductUseCase`) injected directly by REST resources and messaging handlers; use cases may compose other use cases (e.g. `UpdateProductUseCase` calls `GetProductByIdUseCase`) instead of duplicating repository lookups.
- Domain aggregates use Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) instead of hand-written boilerplate; a constructor stays hand-written wherever it isn't a pure all-fields assignment (e.g. it calls `super(...)` or computes a field).
- Public IDs are prefixed and validated (`category_*`, `product_*`, `stock_*`, `customer_*`, ...) — see each service's `domain/shared`/`domain/<entity>` id records (`CategoryId`, `ProductId`, `StockId`, `ReassignmentId`).
- Domain events (e.g. `category.created`, `product.updated`) are written to an `outbox_entries` table in the **same transaction** as the aggregate write (`@Transactional` service methods), guaranteeing atomicity. A `@Scheduled` job in `infra/messaging` (the "relay") polls that table and publishes pending entries to RabbitMQ asynchronously — this replaces the Go version's separate `relay` binary with an in-process job, a deliberate simplification.
- Standardized error responses are produced by JAX-RS `ExceptionMapper`s in `infra/rest/exception`: business errors (400/404/409) return `{"message": "..."}`; bean-validation failures return `{"mensagem": "...", "erros": {...}}`.

### Event Flow

Services publish domain events to the RabbitMQ topic exchange `catalog.events` / `stock.events` after state mutations, via the transactional outbox described above. Other services subscribe to relevant events (e.g. `stock-service` reacts to `product.updated`/`product.deleted`).

### Frontend

Modern.js 3 (React 19, TypeScript). SSR enabled. Linting via Biome (2-space indent, single quotes, 80-char line width). UI components come from `@openlab-ui/react`.

**BFF** — `frontend/api/` (Modern.js requires this exact folder name). Each file under `api/lambda/` is a route under `/api` (`catalog/brands/index.ts` → `/api/catalog/brands`; `[id].ts` → `:id`; export `get`/`post`/`put`/`del`). `api/lib/upstream.ts` calls the backends server-side, using `CATALOG_SERVICE_URL` / `STOCK_SERVICE_URL` (cluster DNS in `.devops/helm/templates/frontend.yaml`, localhost by default). The project is ESM (`"type": "module"`), so relative imports inside `api/` must use the `.ts` extension (`from '../../../lib/upstream.ts'`): `modern dev` loads the TypeScript directly and cannot resolve `.js` → `.ts`, while `rewriteRelativeImportExtensions` (tsconfig) turns it into `.js` in the build output. The browser only talks to `/api/...`, never to the services. The BFF passes the services' JSON through untouched, and the frontend types mirror it exactly: catalog-service serializes in **snake_case** (`brand_id`, `created_at`, `page_size`), so TypeScript types use those names and there is no mapping layer. Nullable fields (e.g. `updated_at`) are typed `string | null`.

**customer-service** — port 8082, schema `customer`, events on `customer.events`. One `customers` table holds both kinds: `type` is `INDIVIDUAL` (CPF) or `COMPANY` (CNPJ) and is immutable after creation; `document` is stored as digits only, unique across both kinds, and validated with the check-digit algorithm (`domain/customer/Document`). Individuals have no trade name / state registration, companies have no birth date (`CustomerDetails.validated` clears them). Addresses are a separate module (`domain/address`, `application/usecase/address`, `AddressResource`), not part of the `Customer` aggregate: they are managed one by one under `/v1/customers/{customerId}/addresses` (`GET` list, `POST`, `PUT /{addressId}`, `DELETE /{addressId}`), IDs are `address_<ULID>`, a customer has up to 10, and `is_default` marks the default — zero or one per customer (use cases clear the previous default; a partial unique index on `customer_addresses` backs it up). The address use cases call `GetCustomerByIdUseCase` for the 404; deleting a customer removes its addresses via `ON DELETE CASCADE`. The frontend mirrors the split: `features/addresses` (panel) is composed into the customer detail route. `PATCH /v1/customers/{id}/status` toggles ACTIVE/INACTIVE. The BFF reaches it through `CUSTOMER_SERVICE_URL`.

**Yarn** — `@modern-js/render` declares `react-server-dom-rspack` as a peer dependency; npm installs peers automatically but Yarn does not, so it is listed explicitly in `frontend/package.json` (without it `yarn dev` fails with `ESModulesLinkingError ... react-server-dom-rspack/client.browser`).

**Features** — `frontend/src/features/<feature>/` holds everything for one domain (see `features/brands`): `api.ts` (calls to the BFF), `queries.ts` (TanStack Query hooks), `schemas.ts` (zod), `types.ts`, `components/`, and an `index.ts` that is the feature's public API. `src/routes/*/page.tsx` stay thin and import only from a feature's `index.ts`. Shared code lives in `src/lib` (HTTP client, utilities) and `src/components` (UI primitives). Forms use react-hook-form + zod; server data uses TanStack Query. Dialogs must stay mounted and be driven by `open`, not unmounted while open.

## Environment Configuration

Each Java service reads Postgres/RabbitMQ settings from environment variables (with local-dev defaults in `application.yaml`):

```
RABBITMQ_HOST / RABBITMQ_PORT / RABBITMQ_USER / RABBITMQ_PASSWORD
POSTGRES_HOST / POSTGRES_PORT / POSTGRES_DATABASE / POSTGRES_USER / POSTGRES_PASSWORD
```

Local-dev defaults (user/password for Postgres and RabbitMQ) are in each service's `application.yaml`.

## Tooling Notes

- **Maven / Quarkus** — `mvn quarkus:dev` for hot reload, `mvn package` to build `target/quarkus-app/quarkus-run.jar`. Requires a Java 21 JDK on `JAVA_HOME`/`PATH`.
- **Flyway** — owns the Postgres schema for each service; migrations live in `src/main/resources/db/migration`.
- **smallrye-openapi / swagger-ui** — OpenAPI docs served at `/docs` on each service (equivalent to the old `swag`-generated docs).
- **Biome** — Frontend lint/format; run `biome check` in `frontend`.
