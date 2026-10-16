CREATE SCHEMA IF NOT EXISTS crm;

CREATE TABLE IF NOT EXISTS crm.outbox_entries (
    outbox_entry_id  BIGSERIAL    PRIMARY KEY,
    routing_key      TEXT         NOT NULL,
    payload          TEXT         NOT NULL,
    status           TEXT         NOT NULL DEFAULT 'pending',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at     TIMESTAMPTZ,
    error            TEXT
);

CREATE INDEX IF NOT EXISTS idx_crm_outbox_status_created ON crm.outbox_entries (status, created_at);
