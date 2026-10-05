CREATE SCHEMA IF NOT EXISTS catalog;

CREATE TABLE IF NOT EXISTS catalog.categories (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    description  TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_categories_created_at ON catalog.categories (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_categories_description_lower ON catalog.categories (lower(description));

CREATE TABLE IF NOT EXISTS catalog.products (
    id                  BIGSERIAL    PRIMARY KEY,
    public_id           TEXT         NOT NULL UNIQUE,
    description         TEXT         NOT NULL,
    short_description   TEXT         NOT NULL,
    unit_of_measure     TEXT         NOT NULL,
    status              TEXT         NOT NULL,
    category_id         BIGINT       NOT NULL REFERENCES catalog.categories (id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_products_created_at ON catalog.products (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON catalog.products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON catalog.products (status);

CREATE TABLE IF NOT EXISTS catalog.outbox_entries (
    id            BIGSERIAL    PRIMARY KEY,
    routing_key   TEXT         NOT NULL,
    payload       TEXT         NOT NULL,
    status        TEXT         NOT NULL DEFAULT 'pending',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at  TIMESTAMPTZ,
    error         TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON catalog.outbox_entries (status, created_at);
