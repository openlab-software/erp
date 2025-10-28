package rabbitmq

import (
	"encoding/json"

	"github.com/streadway/amqp"
)

type RabbitMQPublisher struct {
	conn     *amqp.Connection
	channel  *amqp.Channel
	exchange string
}

func NewRabbitMQPublisher(exchange string) (*RabbitMQPublisher, error) {
	conn, err := connect()
	if err != nil {
		return nil, err
	}

	channel, err := conn.Channel()
	if err != nil {
		return nil, err
	}

	if err := channel.ExchangeDeclare(
		exchange, // name
		"topic",  // type
		true,     // durable
		false,    // auto-deleted
		false,    // internal
		false,    // no-wait
		nil,      // arguments
	); err != nil {
		return nil, err
	}

	return &RabbitMQPublisher{channel: channel, conn: conn, exchange: exchange}, nil
}

func (pub *RabbitMQPublisher) Close() error {
	if err := pub.channel.Close(); err != nil {
		return err
	}
	if err := pub.conn.Close(); err != nil {
		return err
	}
	return nil
}

func (r *RabbitMQPublisher) Publish(routingKey string, body any) error {
	payload, err := json.Marshal(body)
	if err != nil {
		return err
	}

	return r.channel.Publish(
		r.exchange, // exchange
		routingKey, // routing key
		false,      // mandatory
		false,      // immediate
		amqp.Publishing{
			ContentType: "application/json",
			Body:        payload,
		},
	)
}
