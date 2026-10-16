{{- /*
  Módulos do ERP. A fonte de verdade são os manifestos modules/<nome>/module.json do repositório; a CI (ou
  `node scripts/modules.mjs stage`) os copia para files/modules/<nome>.json antes do helm lint/template/package.

  erp.manifests         => { <módulo>: <manifesto> } de todos os módulos do chart (JSON)
  erp.modules.enabled   => lista (JSON) dos módulos ligados em .Values.modules mais as dependências obrigatórias
                           (`depends`), que são ligadas sozinhas, como no Odoo. `integrates_with` NÃO liga nada:
                           é integração opcional, ativa só se o outro módulo também estiver instalado.
  erp.services.enabled  => lista (JSON) dos serviços dos módulos ligados: [{ name, port, bff_prefix, module }]
*/ -}}
{{- define "erp.manifests" -}}
{{- $manifests := dict }}
{{- range $path, $_ := .Files.Glob "files/modules/*.json" }}
{{- $manifest := $.Files.Get $path | fromJson }}
{{- $_ := set $manifests $manifest.name $manifest }}
{{- end }}
{{- if not $manifests }}
{{- fail "files/modules/*.json não encontrado: rode `node scripts/modules.mjs stage` antes do helm lint/template/package" }}
{{- end }}
{{- toJson $manifests }}
{{- end }}

{{- define "erp.modules.enabled" -}}
{{- $manifests := include "erp.manifests" . | fromJson }}
{{- $enabled := dict }}
{{- range $name, $cfg := .Values.modules }}
{{- if not (hasKey $manifests $name) }}
{{- fail (printf "values.yaml: módulo desconhecido em `modules`: %s (existentes: %s)" $name (keys $manifests | sortAlpha | join ", ")) }}
{{- end }}
{{- if $cfg.enabled }}
{{- $_ := set $enabled $name true }}
{{- end }}
{{- end }}
{{- /* fecho das dependências: no máximo uma rodada por módulo existente */ -}}
{{- range $round := until (len $manifests) }}
{{- range $name := keys $enabled }}
{{- range $dep := (get $manifests $name).depends | default list }}
{{- $_ := set $enabled $dep true }}
{{- end }}
{{- end }}
{{- end }}
{{- keys $enabled | sortAlpha | toJson }}
{{- end }}

{{- define "erp.services.enabled" -}}
{{- $manifests := include "erp.manifests" . | fromJson }}
{{- $services := list }}
{{- range $module := include "erp.modules.enabled" . | fromJsonArray }}
{{- $manifest := index $manifests $module }}
{{- range $svc := $manifest.services | default list }}
{{- $services = append $services (merge (dict "module" $module) $svc) }}
{{- end }}
{{- end }}
{{- toJson $services }}
{{- end }}

{{- /* manifestos (JSON) dos módulos ligados, na ordem alfabética: é o que o frontend recebe em ERP_MODULES_JSON */ -}}
{{- define "erp.modules.manifests" -}}
{{- $manifests := include "erp.manifests" . | fromJson }}
{{- $list := list }}
{{- range $module := include "erp.modules.enabled" . | fromJsonArray }}
{{- $list = append $list (get $manifests $module) }}
{{- end }}
{{- toJson $list }}
{{- end }}
