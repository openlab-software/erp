package main

import (
	"context"
	"fmt"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"

	// _ "github.com/patrickdevbr-portfolio/erp/apps/stock-service/docs"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/mongodatabase"
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

	ctx := context.Background()
	mongoClient, err := mongodatabase.Connect(ctx)
	if err != nil {
		fmt.Println(err)
	}
	defer mongoClient.Disconnect(ctx)

	rabbitMQPublisher, err := rabbitmq.NewRabbitMQPublisher()
	if err != nil {
		fmt.Println(err)
	}
	defer rabbitMQPublisher.Close()

	// eventPublisher := amqpevent.NewEventPublisher(rabbitMQPublisher)
	// pageRepo := mongodb.NewPageRepository(mongoClient)
	// pageSvc := services.NewPageService(pageRepo, eventPublisher)

	router.PathPrefix("/docs").Handler(httpSwagger.WrapHandler)

	// v1 := router.PathPrefix("/v1").Subrouter()
	// rest.NewProductRest(v1, pageSvc)

	srv := &http.Server{
		Addr:    app.config.addr,
		Handler: router,
	}

	return srv.ListenAndServe()
}
