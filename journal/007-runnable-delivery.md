# 007 - Entrega executável e pipelines

## Commit

`chore: package and document the runnable lab`

## Objetivo

Tornar o laboratório reproduzível do clone ao smoke, com containers, manifests, CI e documentação
defendível em entrevista.

## Implementacao

- Imagens multi-stage sem root e Compose com PostgreSQL, Redis, API e Angular/Nginx.
- Exemplos equivalentes de Jenkins e GitLab CI; o workflow GitHub Actions foi entregue no incremento anterior.
- Deployment, Service, ConfigMap, molde de Secret e Route OpenShift.
- Smoke idempotente, README operacional, guia de estudo e evidência local observada.

## Rastreabilidade ADR

ADR aplicado: ADR-0001 - PostgreSQL serializa reservas e Redis é derivável.

## Verificacao

- `./mvnw verify` — aprovado: 10 testes, 0 failures/errors/skips.
- `npm --prefix frontend test -- --watch=false` — aprovado: 1 teste.
- `npm --prefix frontend run build` — aprovado: bundle inicial de 285,57 kB.
- `docker compose config --quiet` e `docker compose build --pull=false` — aprovados.
- `./scripts/smoke.sh` — aprovado contra os quatro containers reais.
- Kubeconform strict — 4 recursos válidos, 0 inválidos/erros/skips.
- Gate retrospectivo nos cinco commits publicados e gate staged — aprovados.
- `git diff --cached --check` — aprovado.

## Alternativas e trade-offs

O repositório inclui pipelines Jenkins/GitLab como demonstração portável, mas GitHub Actions é a
automação autoritativa que realmente executa neste repositório.

## Proximo passo

Usar o roteiro do README em entrevistas e evoluir apenas quando uma nova pergunta técnica justificar
outro incremento vertical.
