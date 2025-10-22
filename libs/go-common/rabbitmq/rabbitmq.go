package rabbitmq

import (
	"encoding/json"
	"fmt"
	"net/url"
	"os"

	"github.com/streadway/amqp"
)

func connect() (*amqp.Connection, error) {
	host := os.Getenv("RABBITMQ_HOST")
	port := os.Getenv("RABBITMQ_PORT")
	user := os.Getenv("RABBITMQ_USER")
	password := os.Getenv("RABBITMQ_PASSWORD")

	uri := fmt.Sprintf("amqp://%s:%s@%s:%s", url.QueryEscape(user), url.QueryEscape(password), host, port)

	return amqp.Dial(uri)
}

type RabbitMQPublisher struct {
	conn     *amqp.Connection
	channel  *amqp.Channel
	exchange string
}

func NewRabbitMQPublisher() (*RabbitMQPublisher, error) {
	conn, err := connect()
	if err != nil {
		return nil, err
	}

	channel, err := conn.Channel()
	if err != nil {
		return nil, err
	}

	exchange := "cms.events"
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
