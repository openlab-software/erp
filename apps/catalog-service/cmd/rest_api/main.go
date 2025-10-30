package main

import (
	"log"
)

// @title Catalog Service API
// @version 1.0
// @description Serviço responsável por gerenciar o catálogo de produtos dentro do ERP.
// @description Faz parte da arquitetura de microserviços utilizada no portfólio de Patrick Ribeiro.

// @termsOfService https://github.com/patrickdevbr-portfolio/erp

// @contact.name Patrick Ribeiro
// @contact.url https://patrick.dev.br
// @contact.email contato@patrick.dev.br

// @license.name MIT
// @license.url https://opensource.org/licenses/MIT

// @host localhost:8080
// @BasePath /v1

// @schemes http https

func main() {
	app := &application{
		config: config{addr: ":8080"},
	}

	log.Fatal(app.run())
}
