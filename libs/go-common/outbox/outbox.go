package outbox

import "time"

const (
	statusPending   = "pending"
	statusPublished = "published"
	statusFailed    = "failed"
)

// outboxEntry is the GORM model for the outbox_entries table.
// The schema-qualified table name is supplied at runtime via .Table(name).
type outboxEntry struct {
	ID          uint      `gorm:"primaryKey;autoIncrement"`
	RoutingKey  string    `gorm:"not null"`
	Payload     string    `gorm:"not null"`
	Status      string    `gorm:"not null"`
	CreatedAt   time.Time `gorm:"autoCreateTime"`
	PublishedAt *time.Time
	Error       *string
}
