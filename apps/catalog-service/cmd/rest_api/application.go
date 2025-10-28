package main

import (
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	// _ "github.com/patrickdevbr-portfolio/erp/apps/stock-service/docs"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/application/services"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/amqpevent"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/db"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/rest"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/postgres"
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

	gormDB, err := postgres.Connect()
	if err != nil {
		log.Fatal("postgres", err)
	}

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher(event.CatalogEvents)
	if err != nil {
		log.Fatal("rabbitmq", err)
	}
	defer rabbitMQPublisher.Close()

	eventPublisher := amqpevent.NewEventPublisher(rabbitMQPublisher)

	router.PathPrefix("/docs").Handler(httpSwagger.WrapHandler)

	v1 := router.PathPrefix("/v1").Subrouter()

	categoryRepo := db.NewPostgresCategoryRepository(gormDB)
	categorySvc := services.NewCategoryService(categoryRepo, eventPublisher)
	rest.NewCategoryRest(v1, categorySvc)

	productRepo := db.NewPostgresProductRepository(gormDB)
	services.NewProductService(productRepo, eventPublisher)

	srv := &http.Server{
		Addr:    app.config.addr,
		Handler: router,
	}

	return srv.ListenAndServe()
}
