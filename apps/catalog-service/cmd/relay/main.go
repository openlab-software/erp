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

	if err := outbox.Migrate(gormDB, "catalog"); err != nil {
		log.Fatal("outbox migrate:", err)
	}

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher(event.CatalogEvents)
	if err != nil {
		log.Fatal("rabbitmq:", err)
	}
	defer rabbitMQPublisher.Close()

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	outbox.NewRelay(gormDB, rabbitMQPublisher, "catalog", 5*time.Second).Start(ctx)
	log.Println("catalog outbox relay running")

	<-ctx.Done()
	log.Println("catalog outbox relay shutting down")
}
