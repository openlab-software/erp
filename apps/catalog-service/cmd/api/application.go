package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	_ "github.com/openlab-software/erp/apps/catalog-service/cmd/api/docs"
	"github.com/openlab-software/erp/apps/catalog-service/internal/application/services"
	"github.com/openlab-software/erp/apps/catalog-service/internal/infra/postgres"
	"github.com/openlab-software/erp/apps/catalog-service/internal/infra/rest"
	"github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/outbox"
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

	if err := outbox.Migrate(gormDB, "catalog"); err != nil {
		log.Fatal("outbox migrate", err)
	}

	eventPublisher := outbox.NewOutboxPublisher(gormDB, "catalog")
	txManager := db.NewTxManager(gormDB)

	categoryRepo := postgres.NewPostgresCategoryRepository(gormDB)
	productRepo := postgres.NewPostgresProductRepository(gormDB)

	categorySvc := services.NewCategoryService(categoryRepo, eventPublisher, txManager)
	productSvc := services.NewProductService(productRepo, eventPublisher, txManager)

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
