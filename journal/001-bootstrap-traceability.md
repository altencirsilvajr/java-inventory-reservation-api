# 001 - Bootstrap de rastreabilidade

## Commit

`chore: bootstrap tracked development`

## Objetivo

Estabelecer a visão, as decisões duráveis e o processo auditável antes do primeiro código funcional.

## Implementacao

- SDD ativa com limites, contratos planejados e critérios de aceite.
- Processo, instruções do repositório e gate executável.
- ADR que fixa PostgreSQL como autoridade e Redis como cache derivável.

## Rastreabilidade ADR

Novo ADR criado: ADR-0001 - PostgreSQL serializa reservas e Redis é derivável.

## Verificacao

- `./scripts/traceability-gate.sh --staged` — aprovado para o bootstrap com exatamente um Journal.
- `git diff --cached --check` — aprovado sem erros de whitespace.

## Alternativas e trade-offs

O processo foi incorporado ao repositório em vez de depender de convenção externa, tornando o
histórico verificável por colaboradores e CI.

## Proximo passo

Criar o esqueleto Spring Boot e modelar o domínio test-first.
