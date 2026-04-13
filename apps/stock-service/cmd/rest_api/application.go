package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	_ "github.com/patrickdevbr-portfolio/erp/apps/stock-service/cmd/rest_api/docs"

	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/application/services"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/infra/amqpevent"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/infra/postgres"
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/infra/rest"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/db"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/rabbitmq"
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

	// oidcProvider, err := auth.NewOIDCProvider()
	// if err != nil {
	// 	fmt.Println(err)
	// }
	// auth.NewMiddleware(oidcProvider)(mux)

	gormDB, err := db.Connect()
	if err != nil {
		log.Fatal("postgres", err)
	}

	if err := postgres.Migrate(gormDB); err != nil {
		log.Fatal("postgres migrate", err)
	}

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher(event.StockEvents)
	if err != nil {
		fmt.Println(err)
	}
	defer rabbitMQPublisher.Close()

	rabbitMQSubscriber, err := rabbitmq.NewRabbitMQSubscriber(event.CatalogEvents, "stock-subscriber")
	if err != nil {
		fmt.Println(err)
	}
	defer rabbitMQSubscriber.Close()

	eventPublisher := amqpevent.NewEventPublisher(rabbitMQPublisher)
	eventSubscriber := amqpevent.NewEventSubscriber(rabbitMQSubscriber)

	stockRepo := postgres.NewPostgresStockRepository(gormDB)
	stockSvc := services.NewStockService(stockRepo, eventPublisher, eventSubscriber)

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
