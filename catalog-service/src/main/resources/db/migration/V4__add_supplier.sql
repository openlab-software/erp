CREATE TABLE IF NOT EXISTS catalog.suppliers (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    name         TEXT         NOT NULL,
    document     TEXT         NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_suppliers_created_at ON catalog.suppliers (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_suppliers_name_lower ON catalog.suppliers (lower(name));
