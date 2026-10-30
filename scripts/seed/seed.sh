#!/usr/bin/env bash
# Aplica scripts/seed/seed.sql (dados de demonstração de todos os módulos).
#
#   scripts/seed/seed.sh          # Postgres local (POSTGRES_HOST/PORT/DATABASE/USER/PASSWORD, padrão localhost:5432/erp)
#   scripts/seed/seed.sh --k8s    # Postgres do cluster (kubectl exec em database/postgres-0, banco erp)
#
# As migrations já precisam ter rodado (suba os serviços antes). Rodar de novo não duplica nada.
set -euo pipefail

SQL="$(dirname "$0")/seed.sql"

if [[ "${1:-}" == "--k8s" ]]; then
  ns="${PG_NAMESPACE:-database}"
  pod="${PG_POD:-postgres-0}"
  kubectl -n "$ns" exec -i "$pod" -- \
    sh -c 'psql -v ON_ERROR_STOP=1 -U "${POSTGRES_USER:-postgres}" -d "${0}"' "${POSTGRES_DATABASE:-erp}" < "$SQL"
else
  PGPASSWORD="${POSTGRES_PASSWORD:-${PGPASSWORD:-}}" psql -v ON_ERROR_STOP=1 \
    -h "${POSTGRES_HOST:-localhost}" -p "${POSTGRES_PORT:-5432}" \
    -U "${POSTGRES_USER:-postgres}" -d "${POSTGRES_DATABASE:-erp}" -f "$SQL"
fi
