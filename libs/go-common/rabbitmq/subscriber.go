package rabbitmq

import (
	"errors"
	"log"
	"sync"

	"github.com/openlab-software/erp/libs/go-common/event"
	"github.com/streadway/amqp"
)

type RabbitMQSubscriber struct {
	conn     *amqp.Connection
	channel  *amqp.Channel
	exchange string
	queue    string
	mutex    sync.Mutex // garante segurança se múltiplos serviços chamarem Subscribe
}

var ErrRequeue = errors.New("requeue message")

func NewRabbitMQSubscriber(exchange, queue string) (*RabbitMQSubscriber, error) {
	conn, err := connect()
	if err != nil {
		return nil, err
	}

	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, err
	}

	// garante que a exchange exista
	if err := ch.ExchangeDeclare(
		exchange,
		"topic",
		true,  // durable
		false, // auto-delete
		false, // internal
		false, // no-wait
		nil,
	); err != nil {
		conn.Close()
		return nil, err
	}

	// declara a fila (compartilhada entre pods)
	q, err := ch.QueueDeclare(
		queue,
		true,  // durable
		false, // auto-delete
		false, // exclusive
		false, // no-wait
		nil,
	)
	if err != nil {
		conn.Close()
		return nil, err
	}

	return &RabbitMQSubscriber{
		conn:     conn,
		channel:  ch,
		exchange: exchange,
		queue:    q.Name,
	}, nil
}

func (l *RabbitMQSubscriber) Subscribe(bindings []string, handler event.Handler) error {
	l.mutex.Lock()
	defer l.mutex.Unlock()

	// cria os bindings solicitados
	for _, binding := range bindings {
		if err := l.channel.QueueBind(
			l.queue,
			binding,
			l.exchange,
			false,
			nil,
		); err != nil {
			return err
		}
		log.Printf("[RabbitMQ] Bound queue '%s' to routing key '%s'", l.queue, binding)
	}

	// cria o consumer (cada Subscribe pode ter seu próprio handler)
	msgs, err := l.channel.Consume(
		l.queue,
		"",
		false, // auto-ack manual
		false, // exclusive
		false, // no-local
		false, // no-wait
		nil,
	)
	if err != nil {
		return err
	}

	go func() {
		for delivery := range msgs {
			// Processa cada mensagem em sua própria "sub-rotina" para
			// garantir que panics sejam tratados e o Ack/Nack seja feito
			func(d amqp.Delivery) {
				defer func() {
					if r := recover(); r != nil {
						// Pânico é algo inesperado, geralmente reenfileiramos
						log.Printf("[RabbitMQ] panic recovered: %v", r)
						_ = d.Nack(false, true) // requeue on panic
					}
				}()

				// --- LÓGICA DE ABSTRAÇÃO ---
				// Chama o handler do serviço apenas com o corpo da mensagem
				err := handler(d.Body)

				if err != nil {
					// Handler retornou um erro
					log.Printf("[RabbitMQ] handler failed for routing key '%s': %v", d.RoutingKey, err)

					if errors.Is(err, ErrRequeue) {
						// Handler pediu para reenfileirar (ex: erro transitório)
						_ = d.Nack(false, true) // Nack com requeue
					} else {
						// Erro de negócio ou permanente. Não reenfileirar.
						// (Idealmente, vai para uma Dead Letter Exchange)
						_ = d.Nack(false, false) // Nack sem requeue
					}
				} else {
					// Handler processou com sucesso
					_ = d.Ack(false)
				}

			}(delivery)
		}
	}()

	log.Printf("[RabbitMQ] Subscribing on queue '%s' with bindings %v", l.queue, bindings)
	return nil
}

func (l *RabbitMQSubscriber) Close() error {
	if err := l.channel.Close(); err != nil {
		return err
	}
	if err := l.conn.Close(); err != nil {
		return err
	}
	return nil
}
