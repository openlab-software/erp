# Nome do executável do Air (pode ser "air" se estiver instalado globalmente)
AIR := air

CATALOG_SERVICE_GENERATE_DOCS := swag init -g ./cmd/rest_api/main.go --output ./cmd/rest_api/docs --parseDependency

# Caminho dos apps
CATALOG_SERVICE_DIR := apps/catalog-service
PAGE_SERVICE_DIR := apps/page-service

# Alvo padrão
.DEFAULT_GOAL := help

help:
	@echo "Comandos disponíveis:"
	@echo "make docs     -> gera os docs do swagger"
	@echo "make catalog        -> roda o Catalog Service com air"

# --- Content Service ---
docs:
	@echo "Gerando docs do catalog-service..."
	cd $(CATALOG_SERVICE_DIR) && $(CATALOG_SERVICE_GENERATE_DOCS)

# --- Page Service (exemplo opcional) ---
catalog:
	@echo "Iniciando catalog-service..."
	cd $(CATALOG_SERVICE_DIR) && $(AIR)