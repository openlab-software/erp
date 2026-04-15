package outbox

import (
	"fmt"

	"gorm.io/gorm"
)

// Migrate creates the outbox_entries table in the given schema if it doesn't exist.
// Call this at service startup alongside other DB migrations.
//
//	outbox.Migrate(db, "catalog")  // → catalog.outbox_entries
//	outbox.Migrate(db, "stock")    // → stock.outbox_entries
func Migrate(db *gorm.DB, schema string) error {
	sql := fmt.Sprintf(`
		CREATE TABLE IF NOT EXISTS %s.outbox_entries (
			id            BIGSERIAL    PRIMARY KEY,
			routing_key   TEXT         NOT NULL,
			payload       TEXT         NOT NULL,
			status        TEXT         NOT NULL DEFAULT 'pending',
			created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
			published_at  TIMESTAMPTZ,
			error         TEXT
		);
		CREATE INDEX IF NOT EXISTS idx_outbox_%s_status_created
			ON %s.outbox_entries (status, created_at);
	`, schema, schema, schema)

	return db.Exec(sql).Error
}
