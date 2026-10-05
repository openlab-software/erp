CREATE TABLE IF NOT EXISTS stock.movement_reasons (
    id            BIGSERIAL PRIMARY KEY,
    public_id     VARCHAR(80) NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL UNIQUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_movement_reasons_description_lower ON stock.movement_reasons (LOWER(description));
