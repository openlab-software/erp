package event

import "time"

type AnyEvent struct {
	Event     string      `json:"event"`
	Timestamp time.Time   `json:"timestamp"`
	Payload   interface{} `json:"payload"`
}

type Publisher interface {
	Publish(e Event) error
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
