package event

import "time"

const (
	CatalogEvents = "catalog.events"
	StockEvents   = "stock.events"
)

type Handler func(body []byte) error

type Publisher interface {
	Publish(e Event) error
}

type Subscriber interface {
	Subscribe(bindings []string, handler Handler) error
}

type Event struct {
	Event     string    `json:"event"`
	Timestamp time.Time `json:"timestamp"`
	Payload   any       `json:"payload"`
}

func NewEvent(eventName string, payload any) Event {
	return Event{
		Event:     eventName,
		Timestamp: time.Now(),
		Payload:   payload,
	}
}
