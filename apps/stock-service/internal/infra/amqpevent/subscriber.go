package amqpevent

import (
	"github.com/openlab-software/erp/libs/go-common/event"
	"github.com/openlab-software/erp/libs/go-common/rabbitmq"
)

type EventSubscriber struct {
	rabbitmq *rabbitmq.RabbitMQSubscriber
}

func NewEventSubscriber(rabbitmq *rabbitmq.RabbitMQSubscriber) event.Subscriber {
	return &EventSubscriber{rabbitmq: rabbitmq}
}

func (p *EventSubscriber) Subscribe(bindings []string, handler event.Handler) error {
	return p.rabbitmq.Subscribe(bindings, handler)
}
