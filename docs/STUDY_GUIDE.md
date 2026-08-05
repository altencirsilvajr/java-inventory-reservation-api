# Guia de estudo e entrevista

## Roteiro de demonstração

1. Suba o Compose e abra a UI; crie uma SKU com cinco unidades.
2. Reserve três unidades e repita a mesma chave. Explique por que a segunda resposta tem
   `replayed: true` e o saldo muda uma vez.
3. Cancele a reserva e observe a disponibilidade voltar a cinco.
4. Execute a corrida: dois requests pedem todas as unidades. Um conclui; o outro recebe `409`.
5. Confirme uma pendência e diferencie `onHandQuantity` de `reservedQuantity`.
6. Mostre `/actuator/health`, `/actuator/prometheus`, Swagger e os logs com correlation ID.

## Perguntas que o projeto responde

### Por que não usar Redis como lock?

Porque a decisão e a gravação já pertencem ao PostgreSQL. Um lock distribuído introduziria
lease, fencing e recuperação sem eliminar a transação. O row lock serializa somente concorrentes
da mesma SKU e mantém a invariante junto aos dados.

### Concorrência otimista seria errada?

Não. Uma coluna de versão com retentativa limitada também funciona. O laboratório escolhe lock
pessimista porque contenção na unidade final é esperada e a demonstração fica determinística. Em
produção, métricas de contenção e perfil de carga orientariam a escolha.

### Por que dois locks?

O advisory lock protege a unicidade semântica de `Idempotency-Key`, inclusive quando requests
conflitantes citam itens diferentes. O row lock protege a quantidade de uma SKU. A constraint única
permanece como última defesa no banco.

### O cache pode ficar obsoleto?

Sim, por até o TTL se Redis falhar exatamente durante a invalidação. Isso é aceito porque a reserva
nunca consulta Redis para autorizar estoque. Para requisitos de leitura mais fortes, seria possível
versionar a projeção ou desabilitar cache durante degradação.

### Como escalar a expiração?

O scheduler local é suficiente para o laboratório. Em alto volume, a busca usaria lotes com
`SKIP LOCKED`, índice parcial, múltiplos workers e métricas de lag; outra opção é um evento atrasado
durável, sempre com verificação idempotente no banco.

## Pontos para leitura de código

- Entidades: invariantes locais e máquina de estados.
- Serviço de aplicação: fronteiras transacionais e ordem de locks.
- Repositórios: `PESSIMISTIC_WRITE`, query de expiração e advisory lock PostgreSQL.
- Cache invalidator: evento transacional `AFTER_COMMIT`.
- Teste de integração: duas threads reais contra PostgreSQL Testcontainers.
