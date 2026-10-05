CREATE TABLE IF NOT EXISTS catalog.units_of_measure (
    id           BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    code         TEXT         NOT NULL,
    description  TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_units_of_measure_code_lower ON catalog.units_of_measure (lower(code));
CREATE INDEX IF NOT EXISTS idx_units_of_measure_created_at ON catalog.units_of_measure (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_units_of_measure_description_lower ON catalog.units_of_measure (lower(description));
