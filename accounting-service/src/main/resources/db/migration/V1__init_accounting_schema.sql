CREATE SCHEMA IF NOT EXISTS accounting;

CREATE TABLE IF NOT EXISTS accounting.outbox_entries (
    outbox_entry_id  BIGSERIAL    PRIMARY KEY,
    routing_key      TEXT         NOT NULL,
    payload          TEXT         NOT NULL,
    status           TEXT         NOT NULL DEFAULT 'pending',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at     TIMESTAMPTZ,
    error            TEXT
);

CREATE INDEX IF NOT EXISTS idx_accounting_outbox_status_created ON accounting.outbox_entries (status, created_at);
