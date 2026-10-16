-- Stock transfers (Odoo "pickings"): receipts, deliveries and internal moves, with backorders.
CREATE SEQUENCE IF NOT EXISTS stock.picking_receipt_seq START 1;
CREATE SEQUENCE IF NOT EXISTS stock.picking_delivery_seq START 1;
CREATE SEQUENCE IF NOT EXISTS stock.picking_internal_seq START 1;

CREATE TABLE IF NOT EXISTS stock.pickings (
    picking_id            BIGSERIAL    PRIMARY KEY,
    public_id             VARCHAR(80)  NOT NULL UNIQUE,
    number                VARCHAR(40)  NOT NULL UNIQUE,
    type                  VARCHAR(20)  NOT NULL,
    state                 VARCHAR(20)  NOT NULL,
    source_stock_id       BIGINT       REFERENCES stock.stocks (stock_id),
    destination_stock_id  BIGINT       REFERENCES stock.stocks (stock_id),
    partner_id            VARCHAR(80),
    partner_name          VARCHAR(255),
    origin_type           VARCHAR(40),
    origin_id             VARCHAR(80),
    origin_number         VARCHAR(80),
    backorder_of          VARCHAR(80),
    scheduled_date        DATE,
    note                  TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ,
    done_at               TIMESTAMPTZ,
    CONSTRAINT chk_pickings_type  CHECK (type IN ('RECEIPT', 'DELIVERY', 'INTERNAL')),
    CONSTRAINT chk_pickings_state CHECK (state IN ('DRAFT', 'CONFIRMED', 'DONE', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_stock_pickings_state ON stock.pickings (state);
CREATE INDEX IF NOT EXISTS idx_stock_pickings_origin ON stock.pickings (origin_type, origin_id);
CREATE INDEX IF NOT EXISTS idx_stock_pickings_created_at ON stock.pickings (created_at DESC);

CREATE TABLE IF NOT EXISTS stock.picking_lines (
    picking_line_id             BIGSERIAL    PRIMARY KEY,
    picking_id                  BIGINT       NOT NULL REFERENCES stock.pickings (picking_id) ON DELETE CASCADE,
    position                    INT          NOT NULL,
    catalog_product_public_id   VARCHAR(80)  NOT NULL,
    description                 VARCHAR(255),
    demand                      INTEGER      NOT NULL,
    done                        INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT chk_picking_lines_demand CHECK (demand > 0)
);

CREATE INDEX IF NOT EXISTS idx_stock_picking_lines_picking ON stock.picking_lines (picking_id, position);

-- Integration events already applied (the broker delivers at least once).
CREATE TABLE IF NOT EXISTS stock.processed_events (
    event_key     TEXT         PRIMARY KEY,
    processed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
