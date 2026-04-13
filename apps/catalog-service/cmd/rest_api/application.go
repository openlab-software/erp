package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	_ "github.com/patrickdevbr-portfolio/erp/apps/catalog-service/cmd/rest_api/docs"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/application/services"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/amqpevent"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/postgres"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/infra/rest"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/db"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/rabbitmq"
	httpSwagger "github.com/swaggo/http-swagger"
)

type application struct {
	config config
}

type httpConfig struct {
	addr string
	path string
}

type config struct {
	http httpConfig
}

func (app *application) run() error {
	godotenv.Load(".env.dev")

	gormDB, err := db.Connect()
	if err != nil {
		log.Fatal("postgres", err)
	}

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher(event.CatalogEvents)
	if err != nil {
		log.Fatal("rabbitmq", err)
	}
	defer rabbitMQPublisher.Close()

	eventPublisher := amqpevent.NewEventPublisher(rabbitMQPublisher)

	categoryRepo := postgres.NewPostgresCategoryRepository(gormDB)
	productRepo := postgres.NewPostgresProductRepository(gormDB)

	categorySvc := services.NewCategoryService(categoryRepo, eventPublisher)
	productSvc := services.NewProductService(productRepo, eventPublisher)

	router := mux.NewRouter()

	router.Handle("/docs", http.RedirectHandler("/docs/index.html", http.StatusMovedPermanently))
	router.PathPrefix("/docs/").Handler(httpSwagger.WrapHandler)
	v1 := router.PathPrefix(fmt.Sprintf("%s/v1", app.config.http.path)).Subrouter()
	rest.NewCategoryRest(v1, categorySvc)
	rest.NewProductRest(v1, productSvc)

	srv := &http.Server{
		Addr:    app.config.http.addr,
		Handler: router,
	}

	return srv.ListenAndServe()
}
