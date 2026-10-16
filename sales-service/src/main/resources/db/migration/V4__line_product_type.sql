-- GOOD lines are delivered by the inventory module; SERVICE lines (freight, installation, ...) never are.
ALTER TABLE sales.sales_order_lines
    ADD COLUMN IF NOT EXISTS product_type TEXT NOT NULL DEFAULT 'GOOD',
    ADD CONSTRAINT chk_sales_order_lines_product_type CHECK (product_type IN ('GOOD', 'SERVICE'));
