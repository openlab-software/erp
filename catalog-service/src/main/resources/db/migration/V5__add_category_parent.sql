ALTER TABLE catalog.categories
    ADD COLUMN parent_category_id BIGINT NULL REFERENCES catalog.categories (id);

CREATE INDEX IF NOT EXISTS idx_categories_parent_category_id ON catalog.categories (parent_category_id);
