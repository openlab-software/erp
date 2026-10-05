-- Requirement 2: Product.unit_of_measure (free text) becomes Product.unit_of_measure_id
-- (FK to Unit_Of_Measure). Public ids must look like the app's ULID public ids
-- (26 Crockford-base32 chars) so rows created by this migration remain readable
-- by the application afterwards.
CREATE OR REPLACE FUNCTION catalog.generate_ulid_like() RETURNS TEXT AS $$
DECLARE
    alphabet TEXT := '0123456789ABCDEFGHJKMNPQRSTVWXYZ';
    result TEXT := '';
    i INT;
BEGIN
    FOR i IN 1..26 LOOP
        result := result || substr(alphabet, (floor(random() * 32) + 1)::int, 1);
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- 1) Create one Unit_Of_Measure per distinct legacy free-text value, reusing an
--    existing one (by code, case-insensitive) if it already matches.
INSERT INTO catalog.units_of_measure (public_id, code, description, created_at)
SELECT 'uom_' || catalog.generate_ulid_like(), upper(trim(distinct_uom.unit_of_measure)), trim(distinct_uom.unit_of_measure), now()
FROM (SELECT DISTINCT unit_of_measure FROM catalog.products) distinct_uom
WHERE NOT EXISTS (
    SELECT 1 FROM catalog.units_of_measure u WHERE lower(u.code) = lower(trim(distinct_uom.unit_of_measure))
);

-- 2) Point every product at the corresponding Unit_Of_Measure.
ALTER TABLE catalog.products ADD COLUMN unit_of_measure_id BIGINT NULL REFERENCES catalog.units_of_measure (id);

UPDATE catalog.products p
SET unit_of_measure_id = u.id
FROM catalog.units_of_measure u
WHERE lower(u.code) = lower(trim(p.unit_of_measure));

-- 3) Drop the legacy free-text column and enforce the FK.
ALTER TABLE catalog.products ALTER COLUMN unit_of_measure_id SET NOT NULL;
ALTER TABLE catalog.products DROP COLUMN unit_of_measure;

CREATE INDEX IF NOT EXISTS idx_products_unit_of_measure_id ON catalog.products (unit_of_measure_id);

DROP FUNCTION catalog.generate_ulid_like();
