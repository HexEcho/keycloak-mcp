{{- define "keycloak-mcp.name" -}}
keycloak-mcp
{{- end -}}

{{- define "keycloak-mcp.fullname" -}}
{{ include "keycloak-mcp.name" . }}
{{- end -}}
