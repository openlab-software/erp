-- A customer now has many addresses (0 or 1 of them is the default), kept in a child table.
CREATE TABLE IF NOT EXISTS customer.customer_addresses (
    customer_address_id  BIGSERIAL    PRIMARY KEY,
    customer_id          BIGINT       NOT NULL REFERENCES customer.customers (customer_id) ON DELETE CASCADE,
    position             INT          NOT NULL DEFAULT 0,
    label                TEXT,
    zip_code             TEXT,
    street               TEXT,
    street_number        TEXT,
    complement           TEXT,
    neighborhood         TEXT,
    city                 TEXT,
    state                TEXT,
    is_default           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer ON customer.customer_addresses (customer_id, position);

-- "0 or 1 default per customer", enforced by the database.
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_addresses_default
    ON customer.customer_addresses (customer_id) WHERE is_default;

-- Carry over the single address the flat columns could hold (the only one becomes the default).
INSERT INTO customer.customer_addresses
    (customer_id, position, zip_code, street, street_number, complement, neighborhood, city, state, is_default)
SELECT customer_id, 0, zip_code, street, street_number, complement, neighborhood, city, state, TRUE
FROM customer.customers
WHERE coalesce(zip_code, street, street_number, complement, neighborhood, city, state) IS NOT NULL;

ALTER TABLE customer.customers
    DROP COLUMN zip_code,
    DROP COLUMN street,
    DROP COLUMN street_number,
    DROP COLUMN complement,
    DROP COLUMN neighborhood,
    DROP COLUMN city,
    DROP COLUMN state;
