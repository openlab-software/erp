-- Integration events already applied (the broker delivers at least once).
CREATE TABLE IF NOT EXISTS accounting.processed_events (
    event_key     TEXT         PRIMARY KEY,
    processed_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
