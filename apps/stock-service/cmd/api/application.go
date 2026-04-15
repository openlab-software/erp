package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	_ "github.com/openlab-software/erp/apps/stock-service/cmd/api/docs"

	"github.com/openlab-software/erp/apps/stock-service/internal/application/services"
	"github.com/openlab-software/erp/apps/stock-service/internal/infra/amqpevent"
	"github.com/openlab-software/erp/apps/stock-service/internal/infra/postgres"
	"github.com/openlab-software/erp/apps/stock-service/internal/infra/rest"
	"github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
	"github.com/openlab-software/erp/libs/go-common/outbox"
	"github.com/openlab-software/erp/libs/go-common/rabbitmq"
	httpSwagger "github.com/swaggo/http-swagger"
)

type application struct {
	config config
}

type config struct {
	addr string
}

func (app *application) run() error {
	godotenv.Load(".env.dev")

	router := mux.NewRouter()

	gormDB, err := db.Connect()
	if err != nil {
		log.Fatal("postgres", err)
	}

	if err := postgres.Migrate(gormDB); err != nil {
		log.Fatal("postgres migrate", err)
	}

	rabbitMQSubscriber, err := rabbitmq.NewRabbitMQSubscriber(event.CatalogEvents, "stock-subscriber")
	if err != nil {
		fmt.Println(err)
	}
	defer rabbitMQSubscriber.Close()

	eventPublisher := outbox.NewOutboxPublisher(gormDB, "stock")
	eventSubscriber := amqpevent.NewEventSubscriber(rabbitMQSubscriber)

	txManager := db.NewTxManager(gormDB)
	stockRepo := postgres.NewPostgresStockRepository(gormDB)
	stockSvc := services.NewStockService(stockRepo, eventPublisher, eventSubscriber, txManager)

	reassignmentRepo := postgres.NewPostgresReassignmentRepository(gormDB)
	reassignmentSvc := services.NewReassignmentService(reassignmentRepo)

	router.Handle("/docs", http.RedirectHandler("/docs/index.html", http.StatusMovedPermanently))
	router.PathPrefix("/docs/").Handler(httpSwagger.WrapHandler)

	v1 := router.PathPrefix("/v1").Subrouter()
	rest.NewStockRest(v1, stockSvc)
	rest.NewReassignmentRest(v1, reassignmentSvc)

	srv := &http.Server{
		Addr:    app.config.addr,
		Handler: router,
	}

	return srv.ListenAndServe()
}
