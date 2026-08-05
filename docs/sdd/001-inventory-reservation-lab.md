# SDD-001 — Laboratório de reserva de estoque

## Status

Ativa.

## Objetivo e limites

Construir um laboratório Java 17, Spring Boot 3.5 e Angular 22 que demonstre como impedir
overselling quando reservas concorrentes disputam a mesma SKU. O escopo cobre cadastro de
estoque, consulta de disponibilidade, reserva idempotente, confirmação, cancelamento e
expiração. Catálogo, pagamento, autenticação e multi-tenancy ficam fora do projeto.

## Arquitetura e fluxo

- PostgreSQL mantém itens e reservas e é a única fonte de verdade.
- Uma transação com lock no item serializa a verificação e a redução da disponibilidade.
- Uma restrição única por `Idempotency-Key` devolve a reserva original em repetições.
- Redis aplica cache-aside somente à consulta e é invalidado após mutações.
- Um job Spring agendado expira pendências e libera o saldo na mesma transação.
- Controllers Spring MVC expõem REST/OpenAPI e uma UI Angular chama os contratos reais.

## Contratos planejados

- `POST /api/inventory/items`: cria uma SKU com saldo inicial.
- `GET /api/inventory/items/{id}/availability`: consulta saldo total, reservado e disponível.
- `POST /api/reservations` com `Idempotency-Key`: cria reserva pendente.
- `GET /api/reservations/{id}`: consulta a reserva.
- `POST /api/reservations/{id}/confirm`: confirma uma pendência.
- `POST /api/reservations/{id}/cancel`: cancela e libera uma pendência.
- `POST /api/reservations/expire`: seam determinístico de demonstração; o job usa o mesmo caso de uso.

Erros usam Problem Details. Estoque insuficiente e transição inválida retornam `409`;
entrada inválida retorna `400`; recursos ausentes retornam `404`.

## Critérios de aceite

- Duas reservas concorrentes para as últimas unidades aceitam no máximo uma.
- Nenhuma mutação persiste saldo disponível negativo.
- Repetir a mesma chave idempotente não duplica reserva nem altera saldo novamente.
- Confirmar preserva o saldo reservado como consumido; cancelar ou expirar o devolve.
- Toda mutação invalida o cache e indisponibilidade do Redis não impede operações.
- API, UI, testes, Compose, observabilidade e CI podem ser demonstrados a partir do repositório.
