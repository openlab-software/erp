package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/joho/godotenv"
	"github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
	"github.com/openlab-software/erp/libs/go-common/outbox"
	"github.com/openlab-software/erp/libs/go-common/rabbitmq"
)

func main() {
	godotenv.Load(".env.dev")

	gormDB, err := db.Connect()
	if err != nil {
		log.Fatal("postgres:", err)
	}

	// Garante a tabela mesmo que o relay suba antes do api.
	if err := outbox.Migrate(gormDB, "stock"); err != nil {
		log.Fatal("outbox migrate:", err)
	}

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher(event.StockEvents)
	if err != nil {
		log.Fatal("rabbitmq:", err)
	}
	defer rabbitMQPublisher.Close()

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	outbox.NewRelay(gormDB, rabbitMQPublisher, "stock", 5*time.Second).Start(ctx)
	log.Println("stock outbox relay running")

	<-ctx.Done()
	log.Println("stock outbox relay shutting down")
}
