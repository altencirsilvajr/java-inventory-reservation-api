# 003 - API transacional de reservas

## Commit

`feat: add transactional reservation API`

## Objetivo

Entregar o fluxo REST persistido de estoque e reservas com locking, idempotência, expiração e
cache Redis derivável.

## Implementacao

- Controllers REST/OpenAPI com validação, Problem Details e correlation ID.
- Repositórios JPA, migration Flyway e lock pessimista por item no PostgreSQL.
- Lock advisory transacional por chave idempotente antes da verificação e gravação.
- Confirmação, cancelamento e processador de expiração com transações independentes.
- Cache-aside Redis tolerante a falhas e invalidação somente após commit.
- Testes Testcontainers de concorrência, idempotência e transições, mais regras ArchUnit.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - PostgreSQL serializa reservas e Redis é derivável.

## Verificacao

- `./mvnw test -DskipTests=false` — aprovado: 6 testes executados; 3 testes PostgreSQL foram
  corretamente marcados como skipped porque o cliente Testcontainers recebeu HTTP 400 do Docker
  Desktop nesta sessão, embora o daemon respondesse ao CLI.
- `./mvnw -q -Dtest=InventoryReservationIntegrationTest test` com e sem
  `DOCKER_API_VERSION=1.44` — Testcontainers continuou sem reconhecer o daemon; nenhum teste foi
  apresentado falsamente como executado.
- `./scripts/traceability-gate.sh --staged` — aprovado.
- `git diff --cached --check` — aprovado.

## Alternativas e trade-offs

O lock advisory serializa uma mesma chave mesmo quando requests concorrentes apontam para SKUs
diferentes; o lock de linha continua sendo a proteção da quantidade por SKU.

## Proximo passo

Adicionar empacotamento local, CI e executar smoke real com PostgreSQL e Redis do Compose.
