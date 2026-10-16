CREATE SEQUENCE IF NOT EXISTS purchase.purchase_order_number_seq START 1;

CREATE TABLE IF NOT EXISTS purchase.purchase_orders (
    purchase_order_id   BIGSERIAL    PRIMARY KEY,
    public_id        TEXT         NOT NULL UNIQUE,
    number           TEXT         NOT NULL UNIQUE,
    state            TEXT         NOT NULL,
    supplier_id      TEXT         NOT NULL,
    supplier_name    TEXT         NOT NULL,
    origin          TEXT,
    currency         TEXT         NOT NULL DEFAULT 'BRL',
    expected_date      DATE,
    ship_to_address TEXT,
    buyer      TEXT,
    note             TEXT,
    receipt_status  TEXT         NOT NULL DEFAULT 'NONE',
    bill_status   TEXT         NOT NULL DEFAULT 'NONE',
    confirmed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    CONSTRAINT chk_purchase_orders_state    CHECK (state IN ('DRAFT', 'SENT', 'CONFIRMED', 'DONE', 'CANCELLED')),
    CONSTRAINT chk_purchase_orders_receipt CHECK (receipt_status IN ('NONE', 'PENDING', 'PARTIAL', 'DONE')),
    CONSTRAINT chk_purchase_orders_invoice  CHECK (bill_status IN ('NONE', 'TO_BILL', 'BILLED'))
);

CREATE INDEX IF NOT EXISTS idx_purchase_orders_state ON purchase.purchase_orders (state);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_supplier ON purchase.purchase_orders (supplier_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_created_at ON purchase.purchase_orders (created_at DESC);

CREATE TABLE IF NOT EXISTS purchase.purchase_order_lines (
    purchase_order_line_id  BIGSERIAL      PRIMARY KEY,
    purchase_order_id       BIGINT         NOT NULL REFERENCES purchase.purchase_orders (purchase_order_id) ON DELETE CASCADE,
    position             INT            NOT NULL,
    product_id           TEXT           NOT NULL,
    sku                  TEXT,
    description          TEXT           NOT NULL,
    quantity             NUMERIC(15, 3) NOT NULL,
    unit_price           NUMERIC(15, 2) NOT NULL,
    discount_percent     NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    tax_percent          NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    received_quantity   NUMERIC(15, 3) NOT NULL DEFAULT 0,
    CONSTRAINT chk_purchase_order_lines_quantity CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_purchase_order_lines_order ON purchase.purchase_order_lines (purchase_order_id, position);
CREATE INDEX IF NOT EXISTS idx_purchase_order_lines_product ON purchase.purchase_order_lines (product_id);
