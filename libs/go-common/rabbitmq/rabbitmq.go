package rabbitmq

import (
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
