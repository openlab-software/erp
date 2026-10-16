-- Contact persons of a customer (the buyer, the finance contact, ...). 0 or 1 of them is the primary contact.
CREATE TABLE IF NOT EXISTS customer.customer_contacts (
    customer_contact_id  BIGSERIAL    PRIMARY KEY,
    public_id            TEXT         NOT NULL UNIQUE,
    customer_id          BIGINT       NOT NULL REFERENCES customer.customers (customer_id) ON DELETE CASCADE,
    position             INT          NOT NULL DEFAULT 0,
    name                 TEXT         NOT NULL,
    job_title            TEXT,
    email                TEXT,
    phone                TEXT,
    notes                TEXT,
    is_primary           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_customer_contacts_customer ON customer.customer_contacts (customer_id, position);

-- "0 or 1 primary contact per customer", enforced by the database.
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_contacts_primary
    ON customer.customer_contacts (customer_id) WHERE is_primary;
