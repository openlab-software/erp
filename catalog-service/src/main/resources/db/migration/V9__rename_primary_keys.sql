-- Primary keys now carry the full "<entity>_id" column name.
-- Renaming a column keeps its PK/FK constraints and indexes intact.
ALTER TABLE catalog.brands RENAME COLUMN id TO brand_id;
ALTER TABLE catalog.categories RENAME COLUMN id TO category_id;
ALTER TABLE catalog.outbox_entries RENAME COLUMN id TO outbox_entry_id;
ALTER TABLE catalog.price_history RENAME COLUMN id TO price_history_id;
ALTER TABLE catalog.product_attributes RENAME COLUMN id TO product_attribute_id;
ALTER TABLE catalog.product_barcodes RENAME COLUMN id TO product_barcode_id;
ALTER TABLE catalog.products RENAME COLUMN id TO product_id;
ALTER TABLE catalog.product_images RENAME COLUMN id TO product_image_id;
ALTER TABLE catalog.suppliers RENAME COLUMN id TO supplier_id;
ALTER TABLE catalog.units_of_measure RENAME COLUMN id TO unit_of_measure_id;
