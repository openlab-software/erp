# Nome do executável do Air (pode ser "air" se estiver instalado globalmente)
AIR := air

GENERATE_DOCS_COMMAND := swag init -g ./cmd/api/main.go --output ./cmd/api/docs --parseDependency

# Caminho dos apps
CATALOG_SERVICE_DIR := apps/catalog-service
STOCK_SERVICE_DIR := apps/stock-service

# Alvo padrão
.DEFAULT_GOAL := help

help:
	@echo "Comandos disponíveis:"
	@echo "make docs     -> gera os docs do swagger"
	@echo "make catalog  -> roda o Catalog Service (api + relay)"
	@echo "make stock    -> roda o Stock Service (api + relay)"

build:
	@echo "Compilando catalog-service..."
	cd $(CATALOG_SERVICE_DIR) && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o ./bin/api ./cmd/api
	@echo "Compilando stock-service..."
	cd $(STOCK_SERVICE_DIR) && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o ./bin/api ./cmd/api

docs:
	@echo "Gerando docs do catalog-service..."
	cd $(CATALOG_SERVICE_DIR) && $(GENERATE_DOCS_COMMAND)
	cd $(STOCK_SERVICE_DIR) && $(GENERATE_DOCS_COMMAND)

catalog:
	@echo "Iniciando catalog-service (api + relay)..."
	@cd $(CATALOG_SERVICE_DIR) && $(AIR) -c .air.api.toml
	@cd $(CATALOG_SERVICE_DIR) && $(AIR) -c .air.relay.toml

stock:
	@echo "Iniciando stock-service (api + relay)..."
	@cd $(STOCK_SERVICE_DIR) && ( $(AIR) -c .air.api.toml & $(AIR) -c .air.relay.toml; wait; )
