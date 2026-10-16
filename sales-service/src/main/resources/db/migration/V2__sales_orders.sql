CREATE SEQUENCE IF NOT EXISTS sales.sales_order_number_seq START 1;

CREATE TABLE IF NOT EXISTS sales.sales_orders (
    sales_order_id   BIGSERIAL    PRIMARY KEY,
    public_id        TEXT         NOT NULL UNIQUE,
    number           TEXT         NOT NULL UNIQUE,
    state            TEXT         NOT NULL,
    customer_id      TEXT         NOT NULL,
    customer_name    TEXT         NOT NULL,
    lead_id          TEXT,
    currency         TEXT         NOT NULL DEFAULT 'BRL',
    valid_until      DATE,
    delivery_address TEXT,
    salesperson      TEXT,
    note             TEXT,
    delivery_status  TEXT         NOT NULL DEFAULT 'NONE',
    invoice_status   TEXT         NOT NULL DEFAULT 'NONE',
    confirmed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    CONSTRAINT chk_sales_orders_state    CHECK (state IN ('DRAFT', 'SENT', 'CONFIRMED', 'DONE', 'CANCELLED')),
    CONSTRAINT chk_sales_orders_delivery CHECK (delivery_status IN ('NONE', 'PENDING', 'PARTIAL', 'DONE')),
    CONSTRAINT chk_sales_orders_invoice  CHECK (invoice_status IN ('NONE', 'TO_INVOICE', 'INVOICED'))
);

CREATE INDEX IF NOT EXISTS idx_sales_orders_state ON sales.sales_orders (state);
CREATE INDEX IF NOT EXISTS idx_sales_orders_customer ON sales.sales_orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_orders_created_at ON sales.sales_orders (created_at DESC);

CREATE TABLE IF NOT EXISTS sales.sales_order_lines (
    sales_order_line_id  BIGSERIAL      PRIMARY KEY,
    sales_order_id       BIGINT         NOT NULL REFERENCES sales.sales_orders (sales_order_id) ON DELETE CASCADE,
    position             INT            NOT NULL,
    product_id           TEXT           NOT NULL,
    sku                  TEXT,
    description          TEXT           NOT NULL,
    quantity             NUMERIC(15, 3) NOT NULL,
    unit_price           NUMERIC(15, 2) NOT NULL,
    discount_percent     NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    tax_percent          NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    delivered_quantity   NUMERIC(15, 3) NOT NULL DEFAULT 0,
    CONSTRAINT chk_sales_order_lines_quantity CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_sales_order_lines_order ON sales.sales_order_lines (sales_order_id, position);
CREATE INDEX IF NOT EXISTS idx_sales_order_lines_product ON sales.sales_order_lines (product_id);
