CREATE TABLE IF NOT EXISTS catalog.brands (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    description  TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_brands_description_lower ON catalog.brands (lower(description));
CREATE INDEX IF NOT EXISTS idx_brands_created_at ON catalog.brands (created_at DESC);
