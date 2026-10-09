CREATE SCHEMA IF NOT EXISTS customer;

-- Individuals (INDIVIDUAL, CPF) and companies (COMPANY, CNPJ) share one table: `document`
-- holds only digits and is unique across both kinds. Address columns are flat and optional.
CREATE TABLE IF NOT EXISTS customer.customers (
    customer_id         BIGSERIAL    PRIMARY KEY,
    public_id           TEXT         NOT NULL UNIQUE,
    type                TEXT         NOT NULL,
    status              TEXT         NOT NULL,
    name                TEXT         NOT NULL,
    trade_name          TEXT,
    document            TEXT         NOT NULL UNIQUE,
    state_registration  TEXT,
    birth_date          DATE,
    email               TEXT,
    phone               TEXT,
    zip_code            TEXT,
    street              TEXT,
    street_number       TEXT,
    complement          TEXT,
    neighborhood        TEXT,
    city                TEXT,
    state               TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ,
    CONSTRAINT chk_customers_type   CHECK (type IN ('INDIVIDUAL', 'COMPANY')),
    CONSTRAINT chk_customers_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_customers_created_at ON customer.customers (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_customers_name_lower ON customer.customers (lower(name));
CREATE INDEX IF NOT EXISTS idx_customers_type ON customer.customers (type);
CREATE INDEX IF NOT EXISTS idx_customers_status ON customer.customers (status);

CREATE TABLE IF NOT EXISTS customer.outbox_entries (
    outbox_entry_id  BIGSERIAL    PRIMARY KEY,
    routing_key      TEXT         NOT NULL,
    payload          TEXT         NOT NULL,
    status           TEXT         NOT NULL DEFAULT 'pending',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at     TIMESTAMPTZ,
    error            TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON customer.outbox_entries (status, created_at);
