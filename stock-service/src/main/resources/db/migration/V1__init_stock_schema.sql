CREATE TABLE IF NOT EXISTS stock.stocks (
    id            BIGSERIAL PRIMARY KEY,
    public_id     VARCHAR(80) NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_stock_stocks_description_lower ON stock.stocks (LOWER(description));

CREATE TABLE IF NOT EXISTS stock.items (
    id                          BIGSERIAL PRIMARY KEY,
    stock_id                    BIGINT NOT NULL REFERENCES stock.stocks (id) ON DELETE CASCADE,
    catalog_product_id          BIGINT,
    catalog_product_public_id   VARCHAR(80) NOT NULL,
    min_value                   INTEGER,
    current_value               INTEGER NOT NULL DEFAULT 0,
    max_value                   INTEGER,
    active                      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_at                 TIMESTAMPTZ,
    UNIQUE (stock_id, catalog_product_public_id)
);

CREATE INDEX IF NOT EXISTS idx_stock_items_catalog_product_public_id ON stock.items (catalog_product_public_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_stock_id ON stock.items (stock_id);

CREATE TABLE IF NOT EXISTS stock.reassignments (
    id              BIGSERIAL PRIMARY KEY,
    public_id       VARCHAR(80) NOT NULL UNIQUE,
    from_stock_id   BIGINT NOT NULL REFERENCES stock.stocks (id),
    to_stock_id     BIGINT NOT NULL REFERENCES stock.stocks (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_reassignments_from_stock ON stock.reassignments (from_stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_reassignments_to_stock ON stock.reassignments (to_stock_id);

CREATE TABLE IF NOT EXISTS stock.reassignment_items (
    id                          BIGSERIAL PRIMARY KEY,
    reassignment_id             BIGINT NOT NULL REFERENCES stock.reassignments (id) ON DELETE CASCADE,
    catalog_product_id          BIGINT,
    catalog_product_public_id   VARCHAR(80) NOT NULL,
    quantity                    INTEGER NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_reassignment_items_reassignment_id ON stock.reassignment_items (reassignment_id);

CREATE TABLE IF NOT EXISTS stock.outbox_entries (
    id            BIGSERIAL PRIMARY KEY,
    routing_key   TEXT NOT NULL,
    payload       TEXT NOT NULL,
    status        TEXT NOT NULL DEFAULT 'pending',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at  TIMESTAMPTZ,
    error         TEXT
);

CREATE INDEX IF NOT EXISTS idx_stock_outbox_status_created ON stock.outbox_entries (status, created_at);
