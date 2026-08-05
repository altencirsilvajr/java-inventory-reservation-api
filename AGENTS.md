# Repository instructions

- Keep production code, identifiers and commits in English; write documentation in PT-BR.
- Work in atomic vertical slices. Every substantive non-merge commit must contain exactly one file changed under `journal/`.
- Add an ADR only for a durable architectural decision and reference it from the increment journal.
- PostgreSQL is the inventory source of truth. Redis is a disposable read cache and must never authorize a reservation.
- Preserve the scope in `docs/sdd/001-inventory-reservation-lab.md`.

## Verification commands

```bash
./scripts/traceability-gate.sh --staged
./mvnw verify
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
docker compose config --quiet
```
