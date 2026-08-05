# 006 - Pipeline GitHub Actions

## Commit

`ci: add github actions pipeline`

## Objetivo

Executar automaticamente rastreabilidade, backend, frontend, manifests e imagens no repositório
público.

## Implementacao

- Gate de exatamente um Journal em cada commit não-merge.
- Java 17/Maven com testes Testcontainers e cache de dependências.
- Node 22.22.3 com instalação limpa, Vitest e build Angular.
- Validação Compose/Kubeconform e build das duas imagens.

## Rastreabilidade ADR

Decisao local sem ADR novo: o workflow automatiza comandos já definidos pelo processo e não muda
uma decisão arquitetural do produto.

## Verificacao

- Ruby Psych carregou `.github/workflows/ci.yml` sem erro de sintaxe.
- O gate retrospectivo aprovou os cinco commits publicados anteriores.
- `git diff --check` — aprovado antes da entrega.

## Alternativas e trade-offs

GitHub Actions é autoritativo porque hospeda o repositório; Jenkins e GitLab permanecem exemplos
portáveis no incremento seguinte.

## Proximo passo

Entregar Compose, manifests, documentação e evidência operacional.
