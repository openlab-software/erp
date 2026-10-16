-- GOOD lines are received by the inventory module; SERVICE lines (freight, installation, ...) never are.
ALTER TABLE purchase.purchase_order_lines
    ADD COLUMN IF NOT EXISTS product_type TEXT NOT NULL DEFAULT 'GOOD',
    ADD CONSTRAINT chk_purchase_order_lines_product_type CHECK (product_type IN ('GOOD', 'SERVICE'));
