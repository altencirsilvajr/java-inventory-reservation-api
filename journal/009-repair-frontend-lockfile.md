# 009 - Reparar lockfile do frontend

## Commit

`fix: synchronize frontend lockfiles`

## Objetivo

Restaurar o lockfile completo do console de inventario.

## Implementacao

- Recupera e regenera o lockfile preservando a arvore auditada.

## Rastreabilidade ADR

Decisao local sem ADR novo: correcao mecanica sem mudar reservas.

## Verificacao

- Lockfile JSON valido; `npm ci` sem warnings.
- Audit: 0 vulnerabilidades; nenhum script pendente.
