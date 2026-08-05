# Java Inventory Reservation API

Laboratório vertical **Java 17 + Spring Boot 3.5 + Angular 22** para demonstrar como
impedir overselling quando reservas concorrentes disputam a mesma SKU. O projeto é pequeno de
propósito: uma evidência de engenharia sênior e um roteiro de entrevista, não um produto comercial.

## O problema demonstrado

PostgreSQL é a fonte de verdade. Toda reserva adquire lock pessimista no item dentro de uma
transação, valida o saldo e grava a pendência atomicamente. Um advisory lock por
`Idempotency-Key` impede duplicação concorrente. Redis acelera somente a consulta de
disponibilidade; uma falha nele degrada performance, mas nunca autoriza estoque.

```text
Angular :4200 ──HTTP──> Spring MVC :5305
                              │
                    application/domain
                     ┌──────┴──────┐
             PostgreSQL :5435   Redis :6385
             fonte de verdade   cache descartável
                     │
             expiration scheduler
```

Fluxos: cadastrar estoque, consultar disponibilidade, reservar com repetição segura, confirmar,
cancelar e expirar pendências. Catálogo, pagamento, autenticação e multi-tenancy estão fora do
escopo; veja a [SDD ativa](docs/sdd/001-inventory-reservation-lab.md).

## Executar

Pré-requisitos: Docker Desktop. O Compose constrói backend e frontend e sobe todas as dependências:

```bash
docker compose up --build -d
./scripts/smoke.sh
```

- UI: <http://localhost:4200>
- OpenAPI/Swagger: <http://localhost:5305/swagger-ui.html>
- Health: <http://localhost:5305/actuator/health>
- Prometheus: <http://localhost:5305/actuator/prometheus>

Para desenvolvimento, suba apenas `postgres redis`, execute `./mvnw spring-boot:run` e, com Node
22.22.3+, execute `npm --prefix frontend start`. O proxy Angular encaminha `/api` para a porta 5305.

## Contratos principais

| Método e rota | Comportamento |
|---|---|
| `POST /api/inventory/items` | Cria SKU e saldo inicial |
| `GET /api/inventory/items/{id}/availability` | Lê disponibilidade por cache-aside |
| `POST /api/reservations` | Exige `Idempotency-Key` e cria pendência |
| `GET /api/reservations/{id}` | Consulta estado e expiração |
| `POST /api/reservations/{id}/confirm` | Consome as unidades reservadas |
| `POST /api/reservations/{id}/cancel` | Libera as unidades reservadas |
| `POST /api/reservations/expire` | Executa o mesmo caso de uso do scheduler |

Erros seguem RFC 9457 Problem Details: `400` para entrada inválida, `404` para ausência e `409`
para estoque insuficiente, chave reaproveitada com outro payload ou transição inválida.

## Verificação

```bash
./mvnw verify
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
docker compose config --quiet
./scripts/traceability-gate.sh --staged
```

JUnit 5 cobre invariantes; Testcontainers executa migrations e concorrência contra PostgreSQL real;
ArchUnit protege limites; Vitest verifica o cliente HTTP Angular. GitHub Actions executa testes e
builds de imagens. `Jenkinsfile` e `.gitlab-ci.yml` mostram o mesmo pipeline em ferramentas comuns
nas vagas Java.

## Kubernetes e OpenShift

`deploy/k8s` inclui ConfigMap, Secret de exemplo, Deployment com probes/security context e Service;
`deploy/openshift` adiciona Route TLS. Antes de aplicar, gere `inventory-secrets` no gerenciador de
segredos do ambiente; `REPLACE_ME` é apenas um molde e não uma credencial.

## Decisões defendíveis em entrevista

- [ADR-0001](docs/adr/ADR-0001-postgresql-lock-and-disposable-cache.md): o banco serializa a
  invariante; Redis pode desaparecer sem causar overselling.
- Invalidação de cache ocorre em `AFTER_COMMIT`, evitando repopular leitura a partir de uma
  transação ainda não confirmada.
- Advisory lock por chave resolve a corrida idempotente global; row lock por item resolve a disputa
  por quantidade. São problemas diferentes e, portanto, locks diferentes.
- Cada reserva expirada é processada em transação nova, limitando rollback e tempo de lock.

Continue pelo [guia de estudo](docs/STUDY_GUIDE.md) e pelo histórico em `journal/`.
