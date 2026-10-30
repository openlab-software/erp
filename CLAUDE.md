# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Java + Node.js monorepo for a **modular** ERP system, modeled on Odoo's apps. The Java/Quarkus (Maven) microservices sit at the repository root (`catalog-service`, `stock-service`, `customer-service`, `crm-service`, `sales-service`, `purchase-service`, `accounting-service`); `frontend` (Modern.js/React, with a BFF in `frontend/api/`) is the Node side, a standalone app with its own `package.json` (there is no workspace root). Services communicate via RabbitMQ events and expose REST APIs.

The unit of delivery is the **module** (`modules/<name>/module.json`): it groups the services that depend on each other, declares what it `depends` on (hard) and `integrates_with` (optional, by events), and what goes in the menu. Modules can be deployed together or apart. See "Modules" below and `modules/README.md`.

`catalog-service` and `stock-service` were migrated big-bang from an earlier Go implementation (DDD + GORM + a shared `go-common` library). That Go code, `go.work`, and `libs/go-common` have been removed from the working tree; the Go history is still recoverable via `git log`/`git show` if ever needed for behavioral reference. Both new services implement the full spec in `.kiro/specs/catalog-stock-crud/requirements.md` (since deleted from the tree; recover it with `git log`) (paginated listings, PUT/PATCH/DELETE with lifecycle and referential-integrity rules, domain events, standardized error responses, transactional outbox).

## Project Layout

```
erp/
├── catalog-service/       # Java/Quarkus microservice (products, categories) — port 8180
├── stock-service/         # Java/Quarkus microservice (stock management) — port 8181
├── customer-service/      # Java/Quarkus microservice (customers, addresses, contact persons) — port 8182
├── crm-service/           # Java/Quarkus microservice (pipeline: leads/opportunities, stages, activities) — port 8183
├── sales-service/         # Java/Quarkus microservice (quotations and sales orders) — port 8184
├── purchase-service/      # Java/Quarkus microservice (RFQs and purchase orders) — port 8185
├── accounting-service/    # Java/Quarkus microservice (customer invoices, vendor bills, payments) — port 8186
├── modules/               # One module.json per module (services, dependencies, menu) + README.md
├── scripts/               # modules.mjs (list/validate/resolve/stage) and new-service.mjs (service scaffolding)
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

### Modules

```bash
node scripts/modules.mjs list        # modules, dependencies, services
node scripts/modules.mjs validate    # dependencies, cycles, duplicate ports, services without a module (CI runs it)
node scripts/modules.mjs resolve crm # dependency closure, install order
node scripts/modules.mjs stage       # copies the manifests to .devops/helm/files/modules (the chart reads them)

# new microservice with the common base (errors, i18n, outbox, RabbitMQ, Flyway, OpenAPI, metrics)
node scripts/new-service.mjs <name>-service --package <pkg> --port <port> --title "Description"
```

### Java Services (all `*-service`)

Each service is a standalone Maven/Quarkus project (Java 21). **Use a Java 21 JDK to build/run** — newer JDKs (e.g. 25) currently break the Quarkus/Hibernate bytecode enhancement step (Byte Buddy incompatibility). Point `JAVA_HOME` at a JDK 21 install before running `mvn`.

```bash
# Hot-reload dev mode (from the service directory)
cd catalog-service && mvn quarkus:dev
cd stock-service && mvn quarkus:dev
cd customer-service && mvn quarkus:dev
cd crm-service && mvn quarkus:dev       # likewise sales-service, purchase-service, accounting-service

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

- **Pipeline** (`.github/workflows/main.yml`): pushing a tag `vX.Y.Z` builds and pushes the image of every service declared by a module plus the frontend (`ghcr.io/openlab-software/<service>:vX.Y.Z`; the `modules` job validates the manifests and produces the service matrix) and publishes the chart `oci://ghcr.io/openlab-software/charts/erp` with version `X.Y.Z` (`appVersion` = the tag, which is the frontend image tag). PRs only build the images and run `helm lint`/`helm template`; nothing is pushed.
- **Chart** (`.devops/helm`): the Quarkus kubernetes extension generates `target/kubernetes/kubernetes.yml` at build time with the image tag already set. CI copies it to `.devops/helm/files/<service>.yaml` (gitignored) before `helm package`, and `templates/java-services.yaml` adjusts it at render time: renames the shared `view-jobs` Role per service, turns the `*-flyway-init` Job into an ArgoCD `Sync` hook with `activeDeadlineSeconds` (`flyway.activeDeadlineSeconds` in `values.yaml`) and sets the release namespace. The frontend and the `erp-secrets` ExternalSecret (`templates/secrets.yaml`) are hand-written templates. Postgres (namespace `database`), RabbitMQ (namespace `messaging`) and the observability resources (the Quarkus `ServiceMonitor` and the Node.js `Instrumentation`, both in namespace `observability`) belong to the `infrastructure` repository, not to this chart. `erp-secrets` reads their password from the same Bitwarden secret the infrastructure uses (`secrets.initialPasswordSecretId`); the services reach them at `postgres.database.svc.cluster.local` and `rabbitmq.messaging.svc.cluster.local`, and the frontend pod points at the shared Instrumentation with `inject-nodejs: "observability/nodejs"`. `modules.<name>.enabled` in `values.yaml` turns each **module** on or off (`templates/_helpers.tpl` resolves `depends` transitively, so enabling `sales` also deploys `contacts` and `products`; an unknown module fails the render; `integrates_with` never enables anything) and the frontend gets one `<PREFIX>_SERVICE_URL` per deployed service plus `ERP_MODULES_JSON` (the enabled manifests, served by the BFF at `/api/modules`). CI also renders each module on its own. To run `helm template` locally, run `node scripts/modules.mjs stage` and put the generated service manifests in `.devops/helm/files/<service>.yaml` first.
- **ArgoCD**: the ApplicationSet in the `infrastructure` repository discovers repositories that have `.devops/helm` and follows the chart with `targetRevision: "*"` (highest stable version). A new release is picked up on the next refresh.
- **Failed sync**: ArgoCD keeps retrying a failed operation with the revision it started with, so a fixed release does not take over by itself. Terminate the operation and sync again (UI, or clear `operation` on the Application and sync the new revision).

## Architecture

### Java Microservices — Internal Structure

All the Java services follow the same Domain-Driven Design layering (mirroring the DDD split used by the original Go services, reimplemented independently per service — there is no shared Java library):

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
- Public IDs are prefixed and validated (`category_*`, `prod_*`, `stock_*`, `customer_*`, `supplier_*`, ...) — see each service's `domain/shared`/`domain/<entity>` id records (`CategoryId`, `ProductId`, `StockId`, `ReassignmentId`).
- Domain events (e.g. `category.created`, `product.updated`) are written to an `outbox_entries` table in the **same transaction** as the aggregate write (`@Transactional` service methods), guaranteeing atomicity. A `@Scheduled` job in `infra/messaging` (the "relay") polls that table and publishes pending entries to RabbitMQ asynchronously (a failed publish keeps the entry `pending` with the last error and stops the batch, so a broker outage delays events instead of dropping them) — this replaces the Go version's separate `relay` binary with an in-process job, a deliberate simplification.
- Standardized error responses are produced by JAX-RS `ExceptionMapper`s in `infra/rest/exception`: business errors (400/404/409) return `{"message": "..."}`; bean-validation failures return `{"mensagem": "...", "erros": {...}}`.

### Event Flow

Services publish domain events to their RabbitMQ topic exchange (`catalog.events`, `stock.events`, `customer.events`, `crm.events`, `sales.events`, `purchase.events`, `accounting.events`) after state mutations, via the transactional outbox described above. Other services subscribe to relevant events (e.g. `stock-service` reacts to `product.updated`/`product.deleted`).

### Modules

Cross-module behavior is **only** events, never HTTP calls between services, and a module that only `integrates_with` another must work without it. The rules that make that true:
- A consumer declares the producer's exchange itself (idempotent) and owns its queue (`<consumer>-subscriber[-<producer>]`), so it starts the same when the producer is not deployed; the queue just stays empty.
- Consumers are idempotent (`<schema>.processed_events`, `INSERT ... ON CONFLICT DO NOTHING`), because delivery is at-least-once. Failures nack without requeue.
- Foreign aggregates are referenced by public id only (`Refs.require("customer", id)` checks the `prefix_<ULID>` format); display names the other service owns are snapshotted into the document (`customer_name`, line `description`, line `product_type`).
- Integration contract (JSON is snake_case): `sales_order.confirmed` / `purchase_order.confirmed` carry the whole order (lines included) and make `stock-service` create a DELIVERY / RECEIPT picking and `accounting-service` a draft invoice / vendor bill; `*_order.cancelled` cancels the pending pickings; `picking.done` (with `origin_type`, `origin_id`, `lines[].quantity` actually moved) updates the delivery / receipt status of the order; `invoice.posted` (with `kind`, `origin_type`, `origin_id`) marks it invoiced / billed. Change those payloads together on both sides.
- A new service goes through `scripts/new-service.mjs`, is registered in a module manifest, and `node scripts/modules.mjs validate` must pass.

### New services (modules crm, sales, purchase, accounting) and inventory transfers

- **crm-service** (schema `crm`): `stages` (kanban columns; default Novo/Qualificado/Proposta/Ganho seeded by V2, `won` marks the closing stage, deleting one in use is a 409), `lost_reasons`, `leads` (LEAD or OPPORTUNITY; status OPEN/WON/LOST; entering a won stage wins it; closed leads are frozen until `/reopen`; `customer_id` is an optional reference; tags in `lead_tags`), `activities` under `/v1/leads/{id}/activities` plus the agenda at `/v1/activities`, and `GET /v1/pipeline` (count, expected and probability-weighted revenue per stage).
- **sales-service** (schema `sales`) and **purchase-service** (schema `purchase`) have the same shape (`purchase` was derived from `sales`): a header + lines document with the state machine DRAFT → SENT → CONFIRMED → DONE (lock) / CANCELLED, numbered S00001 / P00001 from a sequence, amounts always computed from the lines (per line: subtotal rounded after the discount, tax rounded on top). Confirming publishes the full order. `delivery_status`/`receipt_status` (NONE/PENDING/PARTIAL/DONE) and `invoice_status`/`bill_status` only change through the integration events above; a delivered/invoiced order cannot be cancelled. Lines carry `product_type` (GOOD default, SERVICE): service lines never wait for a delivery, and an order of services only has delivery status NONE. Product ids are `prod_<ULID>`, suppliers `supplier_<ULID>`. `GET /v1/sales-orders/summary` (and purchase) gives count and total per state.
- **accounting-service** (schema `accounting`): `invoices` of kind CUSTOMER_INVOICE (partner `customer_*`) or VENDOR_BILL (partner `supplier_*`). DRAFT → POSTED (assigns `INV/00001` / `BILL/00001` from separate sequences, default due date +30 days, frozen) → CANCELLED (only without payments; the number is kept on reset to draft). `POST /v1/invoices/{id}/payments` cannot exceed the residual; payment state NOT_PAID/PARTIAL/PAID follows the payments; totals are denormalized into columns on every write so `GET /v1/accounting/position` (open/overdue/draft per kind) is plain SQL. Drafts are created automatically from confirmed orders (unique per origin).
- **stock-service** now also does Odoo-style transfers: `pickings` (RECEIPT/DELIVERY/INTERNAL, `IN/OUT/INT/00001`, DRAFT → CONFIRMED → DONE / CANCELLED, lines merged per product). Validating one (`POST /v1/pickings/{id}/validate`, optional per-line `done`, `create_backorder` default true) moves the goods through the regular `ApplyStockMovementUseCase` (so history and negative-balance protection are unchanged) and leaves a backorder picking with the remainder. Automatic pickings come from the order events: only goods that have a stock item are included (SERVICE lines and unknown products are skipped), quantities are whole units (fractions round up), and the stock is `stock.integration.default-stock` (`STOCK_DEFAULT_STOCK`) or the first stock created; with no stock the picking is created without one and a user sets it before validating. `GET /v1/replenishment` lists items below their minimum (the min/max of an item are its reordering rule) with a suggested quantity.
- **customer-service** (module `contacts`) also keeps contact persons under `/v1/customers/{customerId}/contacts` (same pattern as addresses; `is_primary` is 0 or 1 per customer, max 20).

### Frontend

Modern.js 3 (React 19, TypeScript). SSR enabled. Linting via Biome (2-space indent, single quotes, 80-char line width). UI components come from `@openlab-ui/react`.

**BFF** — `frontend/api/` (Modern.js requires this exact folder name). Each file under `api/lambda/` is a route under `/api` (`catalog/brands/index.ts` → `/api/catalog/brands`; `[id].ts` → `:id`; export `get`/`post`/`put`/`del`). `api/lib/upstream.ts` calls the backends server-side, using `CATALOG_SERVICE_URL` / `STOCK_SERVICE_URL` (cluster DNS in `.devops/helm/templates/frontend.yaml`, localhost by default). The project is ESM (`"type": "module"`), so relative imports inside `api/` must use the `.ts` extension (`from '../../../lib/upstream.ts'`): `modern dev` loads the TypeScript directly and cannot resolve `.js` → `.ts`, while `rewriteRelativeImportExtensions` (tsconfig) turns it into `.js` in the build output. The browser only talks to `/api/...`, never to the services. The BFF reaches the services of the new modules through `CRM_SERVICE_URL`, `SALES_SERVICE_URL`, `PURCHASE_SERVICE_URL` and `ACCOUNTING_SERVICE_URL` (set only for deployed modules; calling one that is not answers 404 "module not installed"). `api/lib/modules.ts` serves `GET /api/modules` from `ERP_MODULES_JSON` (Kubernetes) or, locally, from `../modules/*/module.json` filtered by `ERP_MODULES`. The BFF passes the services' JSON through untouched, and the frontend types mirror it exactly: catalog-service serializes in **snake_case** (`brand_id`, `created_at`, `page_size`), so TypeScript types use those names and there is no mapping layer. Nullable fields (e.g. `updated_at`) are typed `string | null`.

**customer-service** — port 8182, schema `customer`, events on `customer.events`. One `customers` table holds both kinds: `type` is `INDIVIDUAL` (CPF) or `COMPANY` (CNPJ) and is immutable after creation; `document` is stored as digits only, unique across both kinds, and validated with the check-digit algorithm (`domain/customer/Document`). Individuals have no trade name / state registration, companies have no birth date (`CustomerDetails.validated` clears them). Addresses are a separate module (`domain/address`, `application/usecase/address`, `AddressResource`), not part of the `Customer` aggregate: they are managed one by one under `/v1/customers/{customerId}/addresses` (`GET` list, `POST`, `PUT /{addressId}`, `DELETE /{addressId}`), IDs are `address_<ULID>`, a customer has up to 10, and `is_default` marks the default — zero or one per customer (use cases clear the previous default; a partial unique index on `customer_addresses` backs it up). The address use cases call `GetCustomerByIdUseCase` for the 404; deleting a customer removes its addresses via `ON DELETE CASCADE`. The frontend mirrors the split: `features/addresses` (panel) is composed into the customer detail route. `PATCH /v1/customers/{id}/status` toggles ACTIVE/INACTIVE. The BFF reaches it through `CUSTOMER_SERVICE_URL`.

**Modules in the UI** — the sidebar and the breadcrumbs are built from `/api/modules` (`features/modules`: `useModules`, `ModuleGate`), so only the pages of installed modules show up; every page of a module is wrapped in `<ModuleGate module="…">`. Feature map: `features/orders` (sales and purchase share the screens through an `OrderKind` adapter in `kinds.tsx`), `features/invoices`, `features/pickings` (transfers + replenishment), `features/crm` (drag-and-drop pipeline board), `features/contacts` (panel on the customer page). Shared document pieces (line editor, totals, type-ahead picker, status pill) are in `src/components/document`.

**Yarn** — `@modern-js/render` declares `react-server-dom-rspack` as a peer dependency; npm installs peers automatically but Yarn does not, so it is listed explicitly in `frontend/package.json` (without it `yarn dev` fails with `ESModulesLinkingError ... react-server-dom-rspack/client.browser`).

**Features** — `frontend/src/features/<feature>/` holds everything for one domain (see `features/brands`): `api.ts` (calls to the BFF), `queries.ts` (TanStack Query hooks), `schemas.ts` (zod), `types.ts`, `components/`, and an `index.ts` that is the feature's public API. `src/routes/*/page.tsx` stay thin and import only from a feature's `index.ts`. Shared code lives in `src/lib` (HTTP client, utilities) and `src/components` (UI primitives). Forms use react-hook-form + zod; server data uses TanStack Query. Dialogs must stay mounted and be driven by `open`, not unmounted while open.

## Environment Configuration

Each Java service reads Postgres/RabbitMQ settings from environment variables (with local-dev defaults in `application.yaml`):

```
RABBITMQ_HOST / RABBITMQ_PORT / RABBITMQ_USER / RABBITMQ_PASSWORD
POSTGRES_HOST / POSTGRES_PORT / POSTGRES_DATABASE / POSTGRES_USER / POSTGRES_PASSWORD
STOCK_DEFAULT_STOCK   # stock-service only: stock_<ULID> used by the automatic deliveries / receipts
```

Local-dev defaults (user/password for Postgres and RabbitMQ) are in each service's `application.yaml`.

## Tooling Notes

- **Maven / Quarkus** — `mvn quarkus:dev` for hot reload, `mvn package` to build `target/quarkus-app/quarkus-run.jar`. Requires a Java 21 JDK on `JAVA_HOME`/`PATH`.
- **Flyway** — owns the Postgres schema for each service; migrations live in `src/main/resources/db/migration`.
- **smallrye-openapi / swagger-ui** — OpenAPI docs served at `/docs` on each service (equivalent to the old `swag`-generated docs).
- **Biome** — Frontend lint/format; run `biome check` in `frontend`.
