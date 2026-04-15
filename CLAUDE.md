# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a hybrid Go + Node.js monorepo for an ERP system. It uses Turbo for frontend orchestration and a Go workspace for backend services. Services communicate via RabbitMQ events and expose REST APIs.

## Project Layout

```
erp/
├── apps/
│   ├── catalog-service/   # Go microservice (products, categories) — port 8080
│   ├── stock-service/     # Go microservice (stock management) — port 8081
│   └── frontend/          # Modern.js/React app — port 3000
├── libs/
│   ├── go-common/         # Shared Go packages (db, auth, event, logger, rabbitmq, publicid)
│   └── ts-common/         # Shared TypeScript package (@cms/ts-common)
├── .devops/
│   ├── docker/            # Dockerfiles
│   └── k8s/               # Kubernetes manifests
├── go.work                # Go workspace (catalog-service, stock-service, go-common)
├── package.json           # Node.js workspace root (Yarn)
├── dev.docker-compose.yaml
└── Makefile
```

## Development Commands

### Start Local Infrastructure

```bash
docker compose -f dev.docker-compose.yaml up -d
# Starts: PostgreSQL 15 (5432), RabbitMQ (5672, UI: 15672), PgAdmin (5050)
```

### Go Services (catalog-service & stock-service)

```bash
# Hot reload with Air (from service directory)
cd apps/catalog-service && air
cd apps/stock-service && air

# Or via Makefile
make catalog   # starts catalog-service with air

# Build binary
cd apps/catalog-service && go build -o ./bin/main.exe ./cmd/api

# Generate Swagger docs
make docs      # runs swag init for catalog-service
```

### Frontend

```bash
cd apps/frontend
pnpm install
pnpm dev       # Modern.js dev server
pnpm build
pnpm serve     # preview production build
```

### Monorepo (Turbo)

```bash
yarn dev       # start all frontend workspaces
yarn build     # build all workspaces
```

## Architecture

### Go Microservices — Internal Structure

Both Go services follow Domain-Driven Design layering:

```
internal/
├── domain/<entity>/       # Entities, repository interfaces, domain events
├── application/services/  # Use cases / business logic (*_service_impl.go)
└── infra/
    ├── rest/              # HTTP handlers (Gorilla Mux)
    ├── postgres/          # GORM repository implementations
    └── amqpevent/         # RabbitMQ event publisher adapters
cmd/api/              # main.go entry point + Swagger docs
```

Key patterns:
- Repository interfaces defined in `domain/`, implemented in `infra/postgres/`
- Domain events (e.g., `ProductCreated`) published via `EventPublisher` wrapping RabbitMQ
- Swagger docs are generated into `cmd/api/docs/` via `swag init`

### Shared Go Library (`libs/go-common`)

Packages used across services:
- `db/` — PostgreSQL connection via GORM
- `auth/` — Auth middleware
- `event/` — `Publisher` and `Subscriber` interfaces
- `rabbitmq/` — RabbitMQ client implementations
- `logger/` — Structured logging
- `publicid/` — ID generation utilities
- `audit/` — Audit trail support

### Event Flow

Services publish domain events to RabbitMQ after state mutations. Other services subscribe to relevant events. The event publisher is wired at `cmd/api/main.go` startup using the RabbitMQ connection from `libs/go-common/rabbitmq`.

### Frontend

Modern.js 3 (React 19, TypeScript). SSR enabled. Linting via Biome (2-space indent, single quotes, 80-char line width). Uses `@cms/ts-common` for shared types.

## Environment Configuration

Each Go service reads from `.env.dev` (dev) or `.env.prod` (prod). Key variables:

```
RABBITMQ_HOST / RABBITMQ_PORT / RABBITMQ_USER / RABBITMQ_PASSWORD
POSTGRES_HOST / POSTGRES_DATABASE / POSTGRES_USER / POSTGRES_PASSWORD / POSTGRES_PORT
```

Default dev credentials are in `dev.docker-compose.yaml`.

## Go Workspace

`go.work` ties together `apps/catalog-service`, `apps/stock-service`, and `libs/go-common`. When adding a new Go service, register it in `go.work`. Changes to `libs/go-common` affect all services — verify compatibility in both.

## Tooling Notes

- **Air** (`.air.toml` per service) — Go hot reload; outputs to `./bin/main.exe`
- **swag** — Swagger generation; run `make docs` after changing handler annotations
- **Biome** — Frontend lint/format; run `biome check` in `apps/frontend`
- **Turbo** — Caches build outputs in `.next/**` and `dist/**`; `dev` task is non-cached and persistent
