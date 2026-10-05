-- Requirement 8: free-form product attributes (no lifecycle of their own — the
-- whole set is replaced in block on every write).
CREATE TABLE IF NOT EXISTS catalog.product_attributes (
    id           BIGSERIAL    PRIMARY KEY,
    product_id   BIGINT       NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    name         TEXT         NOT NULL,
    value        TEXT         NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_product_attributes_product_id ON catalog.product_attributes (product_id);

-- Requirement 12: product barcodes — code is unique across ALL products.
CREATE TABLE IF NOT EXISTS catalog.product_barcodes (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    product_id   BIGINT       NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    code         TEXT         NOT NULL UNIQUE,
    type         TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_barcodes_product_id ON catalog.product_barcodes (product_id);

-- Requirement 13: product images — at most one is_primary=true per product,
-- enforced at the application layer.
CREATE TABLE IF NOT EXISTS catalog.product_images (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    product_id   BIGINT       NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    url          TEXT         NOT NULL,
    is_primary   BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON catalog.product_images (product_id);

-- Requirement 11: append-only price change history.
CREATE TABLE IF NOT EXISTS catalog.price_history (
    id           BIGSERIAL     PRIMARY KEY,
    public_id    TEXT          NOT NULL UNIQUE,
    product_id   BIGINT        NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    sale_price   NUMERIC(14,2) NOT NULL,
    cost_price   NUMERIC(14,2) NOT NULL,
    changed_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_price_history_product_id_changed_at ON catalog.price_history (product_id, changed_at DESC);
