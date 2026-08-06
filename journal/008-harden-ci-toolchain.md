# 008 - Endurecer toolchain de CI

## Commit

`ci: eliminate toolchain warnings`

## Objetivo

Remover alertas Angular e avisos de runtime interno das GitHub Actions.

## Implementacao

- Fixa `@hono/node-server` corrigido em 2.1.0.
- Versiona aprovacoes dos scripts de instalacao do toolchain.
- Atualiza Actions e adiciona audit de dependencias ao CI.

## Rastreabilidade ADR

Decisao local sem ADR novo: manutencao reversivel sem alterar a semantica de reservas.

## Verificacao

- `npm audit`: 0 vulnerabilidades e nenhum script pendente.
- Teste frontend: 1 aprovado; build Angular aprovado.
- Workflow validado como YAML e sem Actions antigas.
