-- Addresses are now addressable on their own (POST/PUT/DELETE /customers/{id}/addresses/{addressId}).
ALTER TABLE customer.customer_addresses ADD COLUMN IF NOT EXISTS public_id TEXT;

-- Backfill: "address_" + 26 hex chars (a subset of the ULID alphabet, so the id validates).
UPDATE customer.customer_addresses
SET public_id = 'address_' || upper(substr(md5(random()::text || clock_timestamp()::text || customer_address_id::text), 1, 26))
WHERE public_id IS NULL;

ALTER TABLE customer.customer_addresses ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_addresses_public_id ON customer.customer_addresses (public_id);
