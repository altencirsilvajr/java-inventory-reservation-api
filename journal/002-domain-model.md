# 002 - Modelo de estoque e reserva

## Commit

`feat: model inventory reservation lifecycle`

## Objetivo

Modelar as invariantes que impedem saldo negativo e controlam transições de reservas pendentes.

## Implementacao

- Bootstrap Spring Boot 3.5.7 em Java 17 com Maven Wrapper.
- Entidades JPA de item e reserva com operações de domínio explícitas.
- Testes do saldo reservado, confirmação, liberação, expiração e transições inválidas.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - PostgreSQL serializa reservas e Redis é derivável.

## Verificacao

- `./mvnw -q test` antes da implementação — falhou na compilação porque os tipos do domínio ainda não existiam (red observado).
- `./mvnw -q test` após a implementação — aprovado, 4 testes.
- `./scripts/traceability-gate.sh --staged` — aprovado.
- `git diff --cached --check` — aprovado.

## Alternativas e trade-offs

As mutações ficaram nas entidades em vez de setters públicos, concentrando as invariantes sem
introduzir interfaces artificiais.

## Proximo passo

Persistir o fluxo completo com locking, idempotência e cache Redis derivável.
