# 005 - Dashboard Angular de aprendizagem

## Commit

`feat: add angular reservation dashboard`

## Objetivo

Entregar uma UI Angular funcional que chama a API real e torna visíveis saldo, transições,
idempotência e uma corrida concorrente.

## Implementacao

- Angular 22 standalone com formulários reativos e cliente HTTP tipado.
- Fluxos para criar, reservar, confirmar, cancelar, expirar e disparar duas reservas simultâneas.
- Timeline de respostas e estados do backend sem regras de estoque duplicadas no frontend.
- Proxy de desenvolvimento e Nginx para o container final.
- Teste Vitest do header `Idempotency-Key` e payload de reserva.

## Rastreabilidade ADR

Decisao local sem ADR novo: a UI é apenas o seam pedagógico definido pela SDD e não altera a
arquitetura autoritativa do estoque.

## Verificacao

- `npm --prefix frontend test -- --watch=false` com Node 22.22.3 — aprovado: 1 teste.
- `npm --prefix frontend run build` — aprovado; bundle inicial de 285,57 kB.
- `npm --prefix frontend audit --omit=dev` — 0 vulnerabilidades de produção.
- `./scripts/traceability-gate.sh --staged` — aprovado.
- `git diff --cached --check` — aprovado.

## Alternativas e trade-offs

O estado permanece local ao componente porque existe uma única tela; introduzir store global
aumentaria cerimônia sem criar um seam de produção real.

## Proximo passo

Empacotar API/UI, adicionar pipelines e registrar o smoke end-to-end.
