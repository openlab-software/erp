package outbox

import (
	"context"
	"encoding/json"

	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
	"gorm.io/gorm"
)

// OutboxPublisher implements event.Publisher by persisting events to the
// {schema}.outbox_entries table. When called within a transaction propagated
// via context (db.RunInTx), the insert participates in that transaction —
// guaranteeing that the domain write and the event record are always atomic.
type OutboxPublisher struct {
	db        *gorm.DB
	tableName string
}

// NewOutboxPublisher returns an event.Publisher that writes to {schema}.outbox_entries.
func NewOutboxPublisher(db *gorm.DB, schema string) event.Publisher {
	return &OutboxPublisher{
		db:        db,
		tableName: schema + ".outbox_entries",
	}
}

func (p *OutboxPublisher) Publish(ctx context.Context, e event.Event) error {
	payload, err := json.Marshal(e)
	if err != nil {
		return err
	}

	entry := &outboxEntry{
		RoutingKey: e.Event,
		Payload:    string(payload),
		Status:     statusPending,
	}

	// TxFromContext picks up the *gorm.DB transaction started by db.RunInTx,
	// falling back to the plain connection when there is no active transaction.
	db := commondb.TxFromContext(ctx, p.db)
	return db.WithContext(ctx).Table(p.tableName).Create(entry).Error
}
