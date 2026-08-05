# ADR-0001 — PostgreSQL serializa reservas e Redis é derivável

## Status

Aceito.

## Contexto

Reservas concorrentes precisam decidir sobre as mesmas unidades sem saldo negativo. Redis pode
acelerar leitura, mas transformá-lo em coordenador cria duas fontes de verdade e aumenta a
dificuldade de recuperação.

## Decisão

PostgreSQL será a fonte de verdade. O caso de uso de reserva adquirirá lock pessimista no item
dentro da transação, validará disponibilidade e persistirá item e reserva atomicamente. Redis
armazenará somente uma projeção descartável de disponibilidade por cache-aside.

## Consequências

- A invariância crítica independe de lock distribuído e da saúde do Redis.
- Reservas da mesma SKU são serializadas; SKUs distintas continuam independentes.
- Falha de invalidação pode produzir leitura temporariamente antiga até o TTL, nunca overselling.

## Alternativas rejeitadas

### Lock distribuído no Redis

Rejeitado porque adicionaria coordenação e modos de falha sem melhorar a autoridade transacional.

### Concorrência otimista com retentativas

É válida, mas o lock pessimista torna a disputa e a demonstração determinísticas neste laboratório.
