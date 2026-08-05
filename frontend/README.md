# Interface Angular

Painel Angular 22 que consome exclusivamente a API real. Ele permite criar saldo, reservar com uma
chave idempotente, confirmar/cancelar/expirar e disparar duas reservas concorrentes para observar
uma aceitação e um conflito.

```bash
npm ci
npm start
```

O servidor de desenvolvimento encaminha `/api` para `http://localhost:5305`.
