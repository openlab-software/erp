package outbox

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/openlab-software/erp/libs/go-common/rabbitmq"
	"gorm.io/gorm"
)

// Relay polls {schema}.outbox_entries for pending events and publishes them to
// RabbitMQ, then marks each entry as published or failed.
//
// Run one Relay per service (one per schema). A single goroutine is used, so
// no row-level locking is required. If you ever scale to multiple relay
// instances, add SELECT FOR UPDATE SKIP LOCKED here.
type Relay struct {
	db        *gorm.DB
	pub       *rabbitmq.RabbitMQPublisher
	tableName string
	interval  time.Duration
}

// NewRelay creates a Relay that reads from {schema}.outbox_entries and
// publishes to RabbitMQ on every interval tick.
func NewRelay(db *gorm.DB, pub *rabbitmq.RabbitMQPublisher, schema string, interval time.Duration) *Relay {
	return &Relay{
		db:        db,
		pub:       pub,
		tableName: schema + ".outbox_entries",
		interval:  interval,
	}
}

// Start launches the relay loop in a background goroutine.
// Cancel ctx to stop the relay gracefully.
func (r *Relay) Start(ctx context.Context) {
	go r.run(ctx)
	log.Printf("[Outbox] relay started — table: %s, interval: %s", r.tableName, r.interval)
}

func (r *Relay) run(ctx context.Context) {
	ticker := time.NewTicker(r.interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			log.Printf("[Outbox] relay stopped — table: %s", r.tableName)
			return
		case <-ticker.C:
			r.flush(ctx)
		}
	}
}

func (r *Relay) flush(ctx context.Context) {
	var entries []outboxEntry

	if err := r.db.WithContext(ctx).
		Table(r.tableName).
		Where("status = ?", statusPending).
		Order("created_at").
		Limit(100).
		Find(&entries).Error; err != nil {
		log.Printf("[Outbox] failed to fetch entries from %s: %v", r.tableName, err)
		return
	}

	for i := range entries {
		r.publishEntry(ctx, &entries[i])
	}
}

func (r *Relay) publishEntry(ctx context.Context, e *outboxEntry) {
	// json.RawMessage implements json.Marshaler and returns the bytes as-is,
	// so the payload is not double-encoded by RabbitMQPublisher.Publish.
	err := r.pub.Publish(e.RoutingKey, json.RawMessage(e.Payload))

	now := time.Now()
	var updates map[string]any

	if err != nil {
		errStr := err.Error()
		updates = map[string]any{"status": statusFailed, "error": errStr}
		log.Printf("[Outbox] publish failed — id=%d routing_key=%s: %v", e.ID, e.RoutingKey, err)
	} else {
		updates = map[string]any{"status": statusPublished, "published_at": now}
	}

	if updateErr := r.db.WithContext(ctx).
		Table(r.tableName).
		Where("id = ?", e.ID).
		Updates(updates).Error; updateErr != nil {
		log.Printf("[Outbox] failed to update entry id=%d: %v", e.ID, updateErr)
	}
}
