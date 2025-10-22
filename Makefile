# Nome do executável do Air (pode ser "air" se estiver instalado globalmente)
AIR := air

# Caminho dos apps
CONTENT_SERVICE_DIR := apps/content-service
PAGE_SERVICE_DIR := apps/page-service

# Alvo padrão
.DEFAULT_GOAL := help

help:
	@echo "Comandos disponíveis:"
	@echo "  make content     -> roda o Content Service com air"
	@echo "  make page        -> roda o Page Service com air"
	@echo "  make tidy        -> roda go mod tidy em todos os serviços"
	@echo "  make build-all   -> builda todos os serviços"

# --- Content Service ---
content:
	@echo "🚀 Iniciando Content Service..."
	cd $(CONTENT_SERVICE_DIR) && $(AIR)

# --- Page Service (exemplo opcional) ---
page:
	@echo "🚀 Iniciando Page Service..."
	cd $(PAGE_SERVICE_DIR) && $(AIR)

# --- Go mod tidy em todos ---
tidy:
	@echo "🧹 Executando go mod tidy em todos os serviços..."
	cd $(CONTENT_SERVICE_DIR) && go mod tidy
	cd $(PAGE_SERVICE_DIR) && go mod tidy

# --- Build de todos os serviços ---
build-all:
	@echo "🔨 Buildando todos os serviços..."
	cd $(CONTENT_SERVICE_DIR) && go build -o ../../bin/content-service
	cd $(PAGE_SERVICE_DIR) && go build -o ../../bin/page-service
