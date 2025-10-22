package amqpevent

import (
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/rabbitmq"
)

type EventPublisher struct {
	event.Publisher
	rabbitmq *rabbitmq.RabbitMQPublisher
}

func NewEventPublisher(rabbitmq *rabbitmq.RabbitMQPublisher) event.Publisher {
	return &EventPublisher{rabbitmq: rabbitmq}
}

func (p *EventPublisher) Publish(e event.Event) error {
	return p.rabbitmq.Publish(e.Event, e)
}
