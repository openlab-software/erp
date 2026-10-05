-- Requirement 6: mandatory type (GOOD/SERVICE) — backfill existing rows with the
-- prior implicit behavior (GOOD, physical stock-controlled item), then drop the
-- default so every future insert must supply it explicitly.
ALTER TABLE catalog.products ADD COLUMN type TEXT NOT NULL DEFAULT 'GOOD';
ALTER TABLE catalog.products ALTER COLUMN type DROP DEFAULT;

-- Requirement 4: optional brand.
ALTER TABLE catalog.products ADD COLUMN brand_id BIGINT NULL REFERENCES catalog.brands (id);

-- Requirement 10: optional default supplier.
ALTER TABLE catalog.products ADD COLUMN default_supplier_id BIGINT NULL REFERENCES catalog.suppliers (id);

-- Requirement 11: sale/cost price, default 0.
ALTER TABLE catalog.products ADD COLUMN sale_price NUMERIC(14, 2) NOT NULL DEFAULT 0;
ALTER TABLE catalog.products ADD COLUMN cost_price NUMERIC(14, 2) NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_products_type ON catalog.products (type);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON catalog.products (brand_id);
CREATE INDEX IF NOT EXISTS idx_products_default_supplier_id ON catalog.products (default_supplier_id);
