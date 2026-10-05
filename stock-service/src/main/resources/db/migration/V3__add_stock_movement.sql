CREATE TABLE IF NOT EXISTS stock.movements (
    id                          BIGSERIAL PRIMARY KEY,
    public_id                   VARCHAR(80) NOT NULL UNIQUE,
    stock_id                    BIGINT NOT NULL REFERENCES stock.stocks (id),
    catalog_product_public_id   VARCHAR(80) NOT NULL,
    type                        VARCHAR(20) NOT NULL,
    quantity                    INTEGER NOT NULL,
    resulting_balance           INTEGER NOT NULL,
    reason_id                   BIGINT REFERENCES stock.movement_reasons (id),
    reference                   VARCHAR(255),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_stock_id ON stock.movements (stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_catalog_product_public_id ON stock.movements (catalog_product_public_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_reason_id ON stock.movements (reason_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_created_at ON stock.movements (created_at);
