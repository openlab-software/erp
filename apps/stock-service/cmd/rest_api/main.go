package main

import (
	"log"
)

// @title Content Service API
// @version 1.0
// @description Serviço responsável por gerenciar conteúdos e componentes de páginas dentro do CMS.
// @description Faz parte da arquitetura de microserviços utilizada no portfólio de Patrick Ribeiro.

// @termsOfService https://github.com/patrickribeirodev

// @contact.name Patrick Ribeiro
// @contact.url https://patrickribeiro.dev
// @contact.email contato@patrickribeiro.dev

// @license.name MIT
// @license.url https://opensource.org/licenses/MIT

// @host localhost:8081
// @BasePath /v1

// @schemes http https

func main() {
	app := &application{
		config: config{addr: ":8081"},
	}

	log.Fatal(app.run())
}
