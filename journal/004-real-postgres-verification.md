# 004 - Verificação PostgreSQL real

## Commit

`test: verify expiration and concurrency on postgres`

## Objetivo

Executar sem skips os cenários transacionais no PostgreSQL real e provar a liberação por
expiração com invalidação de cache após commit.

## Implementacao

- Testcontainers 2.0.5 compatível com Docker Engine 29/API 1.55.
- Isolamento explícito dos dados entre casos de integração.
- Relógio controlado para expirar uma pendência e verificar estoque/cache deterministicamente.
- Logging estruturado Logstash nativo do Spring Boot.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - PostgreSQL serializa reservas e Redis é derivável.

## Verificacao

- `./mvnw -q -Dtest=InventoryReservationIntegrationTest test` — red inicialmente: o container era
  compartilhado e encontrou três pendências de casos anteriores quando o teste esperava uma.
- O mesmo comando após limpar os repositórios em `@BeforeEach` — aprovado: 4 testes, 0 failures,
  0 errors, 0 skipped contra PostgreSQL 17.10.
- `./scripts/traceability-gate.sh --staged` — aprovado.
- `git diff --cached --check` — aprovado.

## Alternativas e trade-offs

Um container único por classe reduz tempo da suite; limpeza ordenada antes de cada caso preserva
isolamento sem pagar o custo de quatro inicializações de PostgreSQL.

## Proximo passo

Entregar o painel Angular que chama estes contratos reais.
