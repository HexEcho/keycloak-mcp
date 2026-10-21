# Deployment Guide

## Docker

```bash
docker build -t keycloak-mcp .
docker run -p 8080:8080 \
  -e KEYCLOAK_BASE_URL=http://host.docker.internal:8081 \
  -e KEYCLOAK_AUTH_MODE=PASSWORD \
  -e KEYCLOAK_CLIENT_ID=admin-cli \
  -e KEYCLOAK_USERNAME=admin \
  -e KEYCLOAK_PASSWORD=admin \
  keycloak-mcp
```

## Kubernetes

Apply the manifests in `k8s/` after populating the secret.

Recommended apply order:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.example.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

`k8s/ingress.yaml` includes long read/send timeouts for streamable MCP sessions and forwards correlation and proxy headers.
Kubernetes deployment activates `docker,enterprise` profiles by default.

## Helm

```bash
helm upgrade --install keycloak-mcp helm/keycloak-mcp
```

Set enterprise edge values with:

```bash
helm upgrade --install keycloak-mcp helm/keycloak-mcp \
  --set streamableHttp.enforceJwt=true \
  --set streamableHttp.allowedOrigins=https://mcp.example.com \
  --set streamableHttp.allowedHosts=mcp.example.com \
  --set ingress.host=mcp.example.com
```

Helm deployment template activates `docker,enterprise` profiles by default.

## Scaling

The service is stateless and can be horizontally scaled. Token caching is per instance.
`k8s/hpa.yaml` and Helm `autoscaling` values provide CPU-based horizontal scaling defaults for enterprise traffic.
