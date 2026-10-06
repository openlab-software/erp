SHELL := /bin/bash

# --- Config (override on the command line, e.g. `make kind-up TAG=2.0.0`) ---
CLUSTER      ?= erp
NAMESPACE    ?= erp
TAG          ?= 1.0.0-SNAPSHOT
IMAGE_PREFIX ?= docker.io/openlab
SERVICES     := catalog-service stock-service
IMAGES       := $(SERVICES) frontend
K8S_DIR      := .devops/k8s
DOCKER_DIR   := .devops/docker

# Always target the kind cluster, whatever the current kubectl context is.
KUBECTL := kubectl --context kind-$(CLUSTER) -n $(NAMESPACE)

# Secret values used by the in-cluster Postgres/RabbitMQ and by the services.
POSTGRES_PASSWORD ?= Postgres123!
RABBITMQ_PASSWORD ?= RabbitMQ123!

.PHONY: help catalog stock build \
	kind-up kind-down kind-redeploy kind-cluster kind-namespace kind-secret \
	kind-images kind-load kind-infra kind-deploy kind-frontend kind-restart \
	kind-status kind-logs-catalog kind-logs-stock kind-logs-frontend kind-forward

help:
	@echo "Local dev (Quarkus dev mode):"
	@echo "  make catalog            mvn quarkus:dev for catalog-service"
	@echo "  make stock              mvn quarkus:dev for stock-service"
	@echo "  make build              mvn package for both services"
	@echo ""
	@echo "kind:"
	@echo "  make kind-up            create cluster + infra, build/load images and deploy (everything)"
	@echo "  make kind-redeploy      rebuild images, reload them into kind and redeploy the services"
	@echo "  make kind-down          delete the kind cluster"
	@echo "  make kind-status        pods, jobs, services"
	@echo "  make kind-logs-catalog  follow catalog-service logs"
	@echo "  make kind-logs-stock    follow stock-service logs"
	@echo "  make kind-logs-frontend follow frontend logs"
	@echo "  make kind-forward       port-forward frontend:3000 catalog:8080 stock:8081 rabbitmq-ui:15672"
	@echo ""
	@echo "Individual steps: kind-cluster kind-namespace kind-secret kind-images kind-load kind-infra kind-deploy kind-restart"

# --- Local dev (requires a Java 21 JDK on JAVA_HOME) ---
catalog:
	cd catalog-service && mvn quarkus:dev

stock:
	cd stock-service && mvn quarkus:dev

build:
	cd catalog-service && mvn package
	cd stock-service && mvn package

# --- kind ---
kind-up: kind-cluster kind-namespace kind-secret kind-infra kind-images kind-load kind-deploy kind-frontend
	@echo ""
	@echo "Done. Try: make kind-forward  ->  http://localhost:3000 (frontend), http://localhost:8080/catalog-service/docs and http://localhost:8081/docs"

kind-redeploy: kind-images kind-load kind-deploy kind-frontend kind-restart

kind-down:
	kind delete cluster --name $(CLUSTER)

# Create the cluster only if it does not exist yet.
kind-cluster:
	@kind get clusters | grep -qx '$(CLUSTER)' \
		&& echo "kind cluster '$(CLUSTER)' already exists" \
		|| kind create cluster --name $(CLUSTER)

kind-namespace:
	kubectl --context kind-$(CLUSTER) create namespace $(NAMESPACE) --dry-run=client -o yaml \
		| kubectl --context kind-$(CLUSTER) apply -f -

kind-secret:
	$(KUBECTL) create secret generic erp-secrets \
		--from-literal=postgres-password='$(POSTGRES_PASSWORD)' \
		--from-literal=rabbitmq-password='$(RABBITMQ_PASSWORD)' \
		--dry-run=client -o yaml | $(KUBECTL) apply -f -

# Build from the repository root (the Dockerfiles expect it as the context).
kind-images:
	@set -e; for s in $(IMAGES); do \
		echo ">> building $$s"; \
		docker build -f $(DOCKER_DIR)/Dockerfile.$$s -t $(IMAGE_PREFIX)/$$s:$(TAG) .; \
	done

kind-load:
	@set -e; for s in $(IMAGES); do \
		echo ">> loading $$s into kind"; \
		kind load docker-image $(IMAGE_PREFIX)/$$s:$(TAG) --name $(CLUSTER); \
	done

kind-infra:
	$(KUBECTL) apply -f $(K8S_DIR)/infra.yaml
	$(KUBECTL) rollout status deploy/postgres deploy/rabbitmq --timeout=180s

# Flyway Jobs are immutable, so they are deleted before every apply.
kind-deploy:
	$(KUBECTL) delete job $(addsuffix -flyway-init,$(SERVICES)) --ignore-not-found
	@set -e; for s in $(SERVICES); do $(KUBECTL) apply -f $(K8S_DIR)/$$s.yaml; done
	$(KUBECTL) wait --for=condition=complete $(addprefix job/,$(addsuffix -flyway-init,$(SERVICES))) --timeout=300s
	$(KUBECTL) rollout status $(addprefix deploy/,$(SERVICES)) --timeout=300s

kind-frontend:
	$(KUBECTL) apply -f $(K8S_DIR)/frontend.yaml
	$(KUBECTL) rollout status deploy/frontend --timeout=180s

# Same image tag => pods must be restarted to pick up the freshly loaded image.
kind-restart:
	$(KUBECTL) rollout restart $(addprefix deploy/,$(IMAGES))
	$(KUBECTL) rollout status $(addprefix deploy/,$(IMAGES)) --timeout=300s

kind-status:
	$(KUBECTL) get pods,jobs,svc

kind-logs-catalog:
	$(KUBECTL) logs -f deploy/catalog-service

kind-logs-stock:
	$(KUBECTL) logs -f deploy/stock-service

kind-logs-frontend:
	$(KUBECTL) logs -f deploy/frontend

# Ctrl+C stops all four port-forwards.
kind-forward:
	@trap 'kill 0' INT TERM; \
	$(KUBECTL) port-forward svc/frontend 3000:80 & \
	$(KUBECTL) port-forward svc/catalog-service 8080:80 & \
	$(KUBECTL) port-forward svc/stock-service 8081:80 & \
	$(KUBECTL) port-forward svc/rabbitmq 15672:15672 & \
	wait
