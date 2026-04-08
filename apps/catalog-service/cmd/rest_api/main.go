package main

import (
	"log"
)

// @title Catalog Service API
// @version 1.0
// @description Serviço responsável por gerenciar o catálogo de produtos dentro do ERP.
// @description Faz parte da arquitetura de microserviços utilizada no portfólio de Patrick Ribeiro.

// @termsOfService https://github.com/openlab-software/erp

// @contact.name Patrick Ribeiro
// @contact.url https://patrick.dev.br
// @contact.email contato@patrick.dev.br

// @license.name MIT
// @license.url https://opensource.org/licenses/MIT

// @host localhost:8080
// @BasePath /catalog-service/v1

// @schemes http https

func main() {
	app := &application{
		config: config{
			http: httpConfig{
				addr: ":8080",
				path: "/catalog-service",
			},
		},
	}

	log.Fatal(app.run())
}
