CREATE SEQUENCE IF NOT EXISTS accounting.customer_invoice_number_seq START 1;
CREATE SEQUENCE IF NOT EXISTS accounting.vendor_bill_number_seq START 1;

CREATE TABLE IF NOT EXISTS accounting.invoices (
    invoice_id       BIGSERIAL      PRIMARY KEY,
    public_id        TEXT           NOT NULL UNIQUE,
    kind             TEXT           NOT NULL,
    number           TEXT           UNIQUE,
    state            TEXT           NOT NULL,
    payment_state    TEXT           NOT NULL DEFAULT 'NOT_PAID',
    partner_id       TEXT           NOT NULL,
    partner_name     TEXT           NOT NULL,
    currency         TEXT           NOT NULL DEFAULT 'BRL',
    invoice_date     DATE,
    due_date         DATE,
    reference        TEXT,
    note             TEXT,
    origin_type      TEXT,
    origin_id        TEXT,
    origin_number    TEXT,
    -- denormalized from the lines/payments on every write, so the receivable/payable position is plain SQL
    untaxed_amount   NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount       NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    paid_amount      NUMERIC(15, 2) NOT NULL DEFAULT 0,
    posted_at        TIMESTAMPTZ,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    CONSTRAINT chk_invoices_kind          CHECK (kind IN ('CUSTOMER_INVOICE', 'VENDOR_BILL')),
    CONSTRAINT chk_invoices_state         CHECK (state IN ('DRAFT', 'POSTED', 'CANCELLED')),
    CONSTRAINT chk_invoices_payment_state CHECK (payment_state IN ('NOT_PAID', 'PARTIAL', 'PAID'))
);

CREATE INDEX IF NOT EXISTS idx_invoices_kind_state ON accounting.invoices (kind, state);
CREATE INDEX IF NOT EXISTS idx_invoices_partner ON accounting.invoices (partner_id);
CREATE INDEX IF NOT EXISTS idx_invoices_due_date ON accounting.invoices (due_date);
CREATE INDEX IF NOT EXISTS idx_invoices_created_at ON accounting.invoices (created_at DESC);

-- One automatic invoice per originating document and kind (the event can be redelivered).
CREATE UNIQUE INDEX IF NOT EXISTS uq_invoices_origin
    ON accounting.invoices (kind, origin_type, origin_id) WHERE origin_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS accounting.invoice_lines (
    invoice_line_id   BIGSERIAL      PRIMARY KEY,
    invoice_id        BIGINT         NOT NULL REFERENCES accounting.invoices (invoice_id) ON DELETE CASCADE,
    position          INT            NOT NULL,
    product_id        TEXT,
    description       TEXT           NOT NULL,
    quantity          NUMERIC(15, 3) NOT NULL,
    unit_price        NUMERIC(15, 2) NOT NULL,
    discount_percent  NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    tax_percent       NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    CONSTRAINT chk_invoice_lines_quantity CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice ON accounting.invoice_lines (invoice_id, position);

CREATE TABLE IF NOT EXISTS accounting.payments (
    payment_id   BIGSERIAL      PRIMARY KEY,
    public_id    TEXT           NOT NULL UNIQUE,
    invoice_id   BIGINT         NOT NULL REFERENCES accounting.invoices (invoice_id) ON DELETE CASCADE,
    amount       NUMERIC(15, 2) NOT NULL,
    paid_on      DATE           NOT NULL,
    method       TEXT           NOT NULL,
    reference    TEXT,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_payments_invoice ON accounting.payments (invoice_id, paid_on);
