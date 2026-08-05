# Evidência local — 2026-08-05

## Ambiente observado

- Java: Temurin 17.0.19.
- Node: 22.22.3; npm 10.9.8.
- Docker Desktop: Engine 29.6.2, API 1.55.
- PostgreSQL Testcontainers: 17.10.
- Spring Boot: 3.5.7; Angular: linha 22.

## Testes automatizados

`./mvnw verify` executou 10 testes sem failure, error ou skip: quatro de domínio, dois ArchUnit e
quatro de integração PostgreSQL. Os casos de integração provaram replay idempotente, corrida pelas
últimas unidades, confirmação/cancelamento e expiração com invalidação de cache.

`npm --prefix frontend test -- --watch=false` executou um teste Vitest. O build Angular gerou um
bundle inicial de 285,57 kB. `npm audit --omit=dev` encontrou zero vulnerabilidades de produção.

## Empacotamento e smoke

`docker compose build --pull=false` concluiu os dois builds:

- API: imagem local `43bca2d6caa0`.
- UI: imagem local `74c8fdf54c83`.

Após `docker compose up -d --force-recreate api frontend`, os quatro serviços ficaram `Up`, com
PostgreSQL e Redis `healthy`. `./scripts/smoke.sh` criou item e reserva, repetiu a chave com
`replayed: true`, cancelou e confirmou disponibilidade restaurada; resultado:

```text
smoke: ok item=810b5563-fd5b-4488-9d46-09c9995441fa reservation=6fc4b4e6-c975-4806-9787-b4138160b608
```

Consultas adicionais observaram `<title>Inventory Reservation Lab`, OpenAPI `3.1.0` e health
`{"status":"UP","groups":["liveness","readiness"]}`. Os logs da API foram JSON válido no formato
Logstash e incluíram `correlationId` nas requisições.

## Manifests e governança

- `docker compose config --quiet` foi aprovado.
- Kubeconform 0.7.0 em modo strict: 4 recursos Kubernetes válidos, 0 inválidos/erros/skips.
- Ruby Psych carregou os YAMLs de GitHub Actions e GitLab CI sem erro de sintaxe.
- O gate validou os cinco commits publicados anteriores, cada um com exatamente um Journal.
