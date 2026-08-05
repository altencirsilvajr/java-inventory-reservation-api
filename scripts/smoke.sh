#!/usr/bin/env bash
set -euo pipefail

api_url="${API_URL:-http://localhost:5305}"
sku="SMOKE-$(date +%s)"
idempotency_key="smoke-$(date +%s)-$$"
item_response="$(curl -fsS -X POST "$api_url/api/inventory/items" -H 'Content-Type: application/json' -d "{\"sku\":\"$sku\",\"initialQuantity\":5}")"
item_id="$(printf '%s' "$item_response" | sed -E 's/.*"itemId":"([^"]+)".*/\1/')"

reservation_response="$(curl -fsS -X POST "$api_url/api/reservations" -H 'Content-Type: application/json' -H "Idempotency-Key: $idempotency_key" -d "{\"itemId\":\"$item_id\",\"quantity\":3}")"
reservation_id="$(printf '%s' "$reservation_response" | sed -E 's/.*"id":"([^"]+)".*/\1/')"
replay_response="$(curl -fsS -X POST "$api_url/api/reservations" -H 'Content-Type: application/json' -H "Idempotency-Key: $idempotency_key" -d "{\"itemId\":\"$item_id\",\"quantity\":3}")"
printf '%s' "$replay_response" | grep -q '"replayed":true'

curl -fsS -X POST "$api_url/api/reservations/$reservation_id/cancel" >/dev/null
availability="$(curl -fsS "$api_url/api/inventory/items/$item_id/availability")"
printf '%s' "$availability" | grep -q '"availableQuantity":5'

echo "smoke: ok item=$item_id reservation=$reservation_id"
