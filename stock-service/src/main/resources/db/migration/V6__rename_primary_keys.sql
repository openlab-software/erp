-- Primary keys now carry the full "<entity>_id" column name.
-- Renaming a column keeps its PK/FK constraints and indexes intact.
ALTER TABLE stock.movement_reasons RENAME COLUMN id TO movement_reason_id;
ALTER TABLE stock.outbox_entries RENAME COLUMN id TO outbox_entry_id;
ALTER TABLE stock.reassignments RENAME COLUMN id TO reassignment_id;
ALTER TABLE stock.reassignment_items RENAME COLUMN id TO reassignment_item_id;
ALTER TABLE stock.stock_counts RENAME COLUMN id TO stock_count_id;
ALTER TABLE stock.stocks RENAME COLUMN id TO stock_id;
ALTER TABLE stock.items RENAME COLUMN id TO stock_item_id;
ALTER TABLE stock.movements RENAME COLUMN id TO stock_movement_id;
