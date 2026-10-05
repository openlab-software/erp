CREATE TABLE IF NOT EXISTS stock.stock_counts (
    id                          BIGSERIAL PRIMARY KEY,
    public_id                   VARCHAR(80) NOT NULL UNIQUE,
    stock_id                    BIGINT NOT NULL REFERENCES stock.stocks (id),
    catalog_product_public_id   VARCHAR(80) NOT NULL,
    system_value                INTEGER NOT NULL,
    counted_value               INTEGER NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_stock_counts_stock_id ON stock.stock_counts (stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_stock_counts_catalog_product_public_id ON stock.stock_counts (catalog_product_public_id);
CREATE INDEX IF NOT EXISTS idx_stock_stock_counts_created_at ON stock.stock_counts (created_at);
